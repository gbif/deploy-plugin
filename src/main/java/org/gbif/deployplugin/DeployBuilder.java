package org.gbif.deployplugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernameListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.io.Closer;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Jenkins BuildStep plugin that triggers the deployment of artifact.
 */
public class DeployBuilder extends Builder {

  //Name of this plugin, this will be the named displayed in the menu item.
  private static final String PLUGIN_NAME = "GBIF Deployment";

  //Script that deploys a single job
  private static final String DEPLOY_JOB_SH = "/bin/deploy.sh";

  //Script that deploys a full environments
  private static final String DEPLOY_ENVIRONMENT_SH = "/bin/deploy_environment.sh";

  private Environment environment = Environment.DEV;

  //Optional  "deploy artifact" section
  private final OptionalDeployArtifact optionalDeployArtifact;

  private static final String ERROR_MSG = "Error executing deployment scripts";

  // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
  @DataBoundConstructor
  public DeployBuilder(String environment, OptionalDeployArtifact optionalDeployArtifact) {
    this.environment = Environment.valueOf(environment);
    this.optionalDeployArtifact = optionalDeployArtifact;
  }

  /**
   * This class is required because it's an optional block in the UI.
   */
  public static class OptionalDeployArtifact {

    //Artifact name: groupId-artifactId-version
    private final String fullArtifactName;

    @DataBoundConstructor
    public OptionalDeployArtifact(String fullArtifactName) {
      this.fullArtifactName = fullArtifactName;
    }

    public String getFullArtifactName() {
      return fullArtifactName;
    }

  }

  /**
   * Creates an instance of the freemarker configuration.
   */
  public freemarker.template.Configuration getFreemarkerConf(String basePath) {
    freemarker.template.Configuration freemarkerConf = new freemarker.template.Configuration();
    freemarkerConf.setClassForTemplateLoading(DeployBuilder.class, basePath);
    return freemarkerConf;
  }

  /**
   * Executes a freemarker template and leaves the output in a temp file which is returned.
   */
  private File runTemplate(
    freemarker.template.Configuration freemarkerConf, Map<String, Object> data, String fileName, String fileExtension
  ) throws IOException {
    Closer closer = Closer.create();
    try {
      final Template template =
        freemarkerConf.getTemplate(fileName + (Strings.isNullOrEmpty(fileExtension) ? "" : "." + fileExtension));
      final File tmpFile = File.createTempFile(fileName, fileExtension);
      final Writer tmpOut = closer.register(new FileWriter(tmpFile));
      template.process(data, tmpOut);
      return tmpFile;
    } catch (IOException ex) {
      Throwables.propagate(ex);
    } catch (TemplateException ex) {
      Throwables.propagate(ex);
    } finally {
      closer.close();
    }
    throw new IllegalStateException("Error creating a template");
  }

  /**
   * Creates the data required by the freemarker templates.
   */
  private Map<String, Object> buildTemplateModel(AbstractBuild build) {
    Map<String, Object> data = Maps.newHashMap();
    if (!deployAll()) {
      data.put("artifact", Artifact.fromFullName(optionalDeployArtifact.getFullArtifactName()));
    }
    data.put("buildId", build.getId());
    return data;
  }

  /**
   * Redirects the src InputStream to the desc PrintStream.
   */
  private static void inheritIO(final InputStream src, final PrintStream dest) {
    new Thread(new Runnable() {
      public void run() {
        Scanner sc = new Scanner(src);
        while (sc.hasNextLine()) {
          dest.println(sc.nextLine());
        }
      }
    }).start();
  }

  /**
   * Logs the error using the listener and then  propagates it.
   */
  private static void logAndPropagate(BuildListener listener, Throwable throwable) {
    listener.getLogger().println(ERROR_MSG);
    listener.getLogger().println(throwable);
    Throwables.propagate(throwable);
  }

  @Override
  public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
    try {
      Process process = deployAll() ? runDeployEnv(build, listener) : runSingleDeploy(build, listener);
      return process.waitFor() == 0;
    } catch (Exception e) {
      logAndPropagate(listener, e);
    }
    return false;
  }

  /**
   * Starts the process tha deploys a single artifact.
   */
  private Process runSingleDeploy(AbstractBuild build, BuildListener listener) throws IOException {
    final freemarker.template.Configuration freemarkerConf = getFreemarkerConf("/ansible");
    final Map<String, Object> data = buildTemplateModel(build);
    //Executes the template engine to create the host and variables files
    final File serviceFile = runTemplate(freemarkerConf, data, "service", "yaml");
    final File hostsFile = runTemplate(freemarkerConf, data, "deploy_hosts", "");
    return startProcess(DEPLOY_JOB_SH, listener, build, hostsFile.getAbsolutePath(), serviceFile.getAbsolutePath());
  }

  /**
   * Starts the process that deploys an environment.
   */
  private Process runDeployEnv(AbstractBuild build, BuildListener listener) throws IOException {
    final freemarker.template.Configuration freemarkerConf = getFreemarkerConf("/ansible");
    final Map<String, Object> data = buildTemplateModel(build);
    final File hostsFile = runTemplate(freemarkerConf, data, "deploy_hosts", "");
    return startProcess(DEPLOY_ENVIRONMENT_SH, listener, build, hostsFile.getAbsolutePath());
  }

  /**
   * Starts the execution of deployment script using the listed parameters.
   */
  private Process startProcess(String scriptFile, BuildListener listener, AbstractBuild build, String... params)
    throws IOException {

    if (Strings.isNullOrEmpty(getDescriptor().getCredentialsId())) { //Plugin hasn't been configured yet
      listener.getLogger()
        .println("Git credentials hasn't been defined, please do it in the main Jenkins configuration page");
      throw new IllegalStateException();
    }

    StandardUsernamePasswordCredentials credentials = lookupGitCredentials();

    File ansibleScriptFile = new File(DeployBuilder.class.getResource(scriptFile).getFile());
    //sets execution permissions
    ansibleScriptFile.setExecutable(true, false);

    /**
     * Order of parameters in deploy.sh script: environment, hosts file, services file and buildId.
     * Git credentials are passed in a single string: username:password.
     */
    List<String> command = new ImmutableList.Builder<String>().add(ansibleScriptFile.getAbsolutePath())
      .add(credentials.getUsername() + ':' + credentials.getPassword())
      .add(environment.name().toLowerCase())
      .add(params)
      .add(build.getId())
      .build();

    //Executes the ansible scripts
    Process process = new ProcessBuilder(command).start();
    //Redirects the output and error streams
    inheritIO(process.getInputStream(), listener.getLogger());
    inheritIO(process.getErrorStream(), listener.getLogger());
    return process;
  }

  /**
   * Lookup the Git credentials.
   */
  private StandardUsernamePasswordCredentials lookupGitCredentials() {
    return CredentialsMatchers.firstOrNull(CredentialsProvider.lookupCredentials(StandardUsernamePasswordCredentials.class,
                                                                                 Jenkins.getInstance(),
                                                                                 ACL.SYSTEM,
                                                                                 Collections.<DomainRequirement>emptyList()),
                                           CredentialsMatchers.withId(getDescriptor().getCredentialsId()));
  }

  /**
   * Checks if all the artifacts must be deployed onto the target enviroment.
   */
  private boolean deployAll() {
    return optionalDeployArtifact == null;
  }

  /**
   * Determines which deployment script should be used.
   */
  private String getDeployScript() {
    return deployAll() ? DEPLOY_ENVIRONMENT_SH : DEPLOY_JOB_SH;
  }

  public Environment getEnvironment() {
    return environment;
  }

  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  public OptionalDeployArtifact getOptionalDeployArtifact() {
    return optionalDeployArtifact;
  }

  @Override
  public DeployDescriptor getDescriptor() {
    return (DeployDescriptor) super.getDescriptor();
  }

  /**
   * Plugin descriptor: only provides a name and the ListBoxModels for selection lists.
   */
  @Extension
  public static final class DeployDescriptor extends BuildStepDescriptor<Builder> {

    //Credentials to authenticate against GBIF private repositories
    private String credentialsId;

    @Override
    public boolean isApplicable(Class<? extends AbstractProject> jobType) {
      return true; //Applicable to all projects
    }

    @Override
    public String getDisplayName() {
      return PLUGIN_NAME;
    }

    public String getCredentialsId() {
      return credentialsId;
    }

    public void setCredentialsId(String credentialsId) {
      this.credentialsId = credentialsId;
    }

    public ListBoxModel doFillFullArtifactNameItems() {
      return Artifact.LIST_BOX_MODEL;
    }

    public ListBoxModel doFillEnvironmentItems() {
      return Environment.LIST_BOX_MODEL;
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
      credentialsId = json.getString("credentialsId");
      save();
      return super.configure(req, json);
    }

    public ListBoxModel doFillCredentialsIdItems() {
      return new StandardUsernameListBoxModel().withAll(CredentialsProvider.lookupCredentials(
        StandardUsernamePasswordCredentials.class,
        Jenkins.getInstance(),
        ACL.SYSTEM,
        Collections.<DomainRequirement>emptyList()));
    }

  }

}
