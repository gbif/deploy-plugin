package org.gbif.deployplugin;

import org.gbif.deployplugin.model.ConfigurationEnvironment;
import org.gbif.deployplugin.model.Service;
import org.gbif.deployplugin.model.GitHubServicesReader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernameListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.io.Closer;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.tasks.test.AggregatedTestResultAction;
import hudson.util.ArgumentListBuilder;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.jenkinsci.remoting.RoleChecker;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Jenkins BuildStep plugin that triggers the deployment of artifact.
 */
public class DeployBuilder extends Notifier {

  public enum DeployType {
    SERVICES, VARNISH, BASEINSTALL;
  }

  //Name of this plugin, this will be the named displayed in the menu item.
  private static final String PLUGIN_NAME = "GBIF Deployment";

  //Script that deploys a single job
  private static final String DEPLOY_JOB_SH = "/bin/deploy.sh";

  private static final String ALL_SERVICES_FMT = "../../gbif-configuration/environments/%s/services.yml";

  //Optional  "deploy artifact" section
  private final DeployOption deployOption;

  private Environment environment = Environment.DEV;

  //Branch of https://github.com/gbif/c-deploy/ to use
  private String cdeployBranch = "master";

  private static final String ERROR_MSG = "Error executing deployment scripts";

  // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
  @DataBoundConstructor
  public DeployBuilder(String environment, DeployOption deployOption, String cdeployBranch) {
    this.environment = Environment.valueOf(environment);
    this.deployOption = deployOption;
    this.cdeployBranch = cdeployBranch;
  }

  public static class DeployOption {

    private final OptionalDeployArtifact optionalDeployArtifact;
    private DeployType deployType;

    @DataBoundConstructor
    public DeployOption(String value, @Nullable OptionalDeployArtifact optionalDeployArtifact){
      deployType = DeployType.valueOf(value);
      this.optionalDeployArtifact = optionalDeployArtifact;
    }

    public OptionalDeployArtifact getOptionalDeployArtifact(){
      return  optionalDeployArtifact;
    }

    public String getDeployType() {
      return deployType.name();
    }
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
  private static Configuration getFreemarkerConf(String basePath) {
    Configuration freemarkerConf = new Configuration();
    freemarkerConf.setClassForTemplateLoading(DeployBuilder.class, basePath);
    return freemarkerConf;
  }

  public BuildStepMonitor getRequiredMonitorService() {
    return BuildStepMonitor.BUILD;
  }

  /**
   * Executes a freemarker template and leaves the output in a temp file which is returned.
   */
  private File runTemplate(
    Configuration freemarkerConf, Map<String, Object> data, String fileName, String fileExtension
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
  private Map<String, Object> buildTemplateModel(AbstractBuild<?,?> build) {
    Map<String, Object> data = Maps.newHashMap();
    if (!deployAll()) {
      data.put("artifact", getArtifactToDeploy());
    }
    data.put("buildId", build.getId());
    return data;
  }

  /**
   * Gets the artifact to be deployed when a single artifact is selected.
   * The artifact version is read from the GitHub repository.
   */
  private Artifact getArtifactToDeploy() {
    try {
      Artifact artifact = Artifact.fromFullName(deployOption.getOptionalDeployArtifact().getFullArtifactName());
      ConfigurationEnvironment configurationEnvironment = GitHubServicesReader.getEnvironmentServices(getEnvironment().name()
                                                                                                        .toLowerCase(),
                                                                                                      lookupGitCredentials());
      for (Service service : configurationEnvironment.getServices()) {
        if (artifact.getArtifactId().equals(service.getArtifactId())) {
          return new Artifact(artifact.getGroupId(),
                              artifact.getArtifactId(),
                              service.getClassifier() != null? service.getClassifier() : artifact.getClassifier(),
                              artifact.getPackaging(),
                              service.getVersion(),
                              service.getFramework(),
                              service.getTestOnDeploy().equals("1"),
                              Optional.ofNullable(service.getUseFixedPorts()).orElse("0").equals("1"),
                              service.getHttpPort(),
                              service.getHttpAdminPort());
        }
      }
      return artifact;
    } catch(IOException ex) {
      throw Throwables.propagate(ex);
    }
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
  public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) {
    try {
      //only successful builds are deployed
      if(isSuccessfulBuild(build)) {
        int exitCode =
          deployAll() ? runDeployEnv(build, listener, launcher) : runSingleDeploy(build, listener, launcher);
        return exitCode == 0;
      }
    } catch (Exception e) {
      logAndPropagate(listener, e);
    }
    return false;
  }

  /**
   * Validates if the build is successful: all tests have passed or the build doesn't have tests at all.
   */
  private static boolean isSuccessfulBuild(AbstractBuild<?,?> build){
    AggregatedTestResultAction testResultAction = build.getAction(AggregatedTestResultAction.class);
    return
     (testResultAction == null) ||
     (testResultAction.getTotalCount() > 0 && testResultAction.getFailCount() == 0);
  }

  /**
   * Starts the process tha deploys a single artifact.
   */
  private int runSingleDeploy(AbstractBuild<?,?> build, BuildListener listener, Launcher launcher)
    throws IOException, InterruptedException {
    final Configuration freemarkerConf = getFreemarkerConf("/ansible");
    final Map<String, Object> data = buildTemplateModel(build);
    //Executes the template engine to create the host and variables files
    final File serviceFile = runTemplate(freemarkerConf, data, "service", "yaml");
    final File hostsFile = runTemplate(freemarkerConf, data, "deploy_hosts", "");
    return startProcess(DEPLOY_JOB_SH,
                        listener,
                        launcher,
                        build,
                        hostsFile.getAbsolutePath(),
                        serviceFile.getAbsolutePath(),
                        deployOption.getDeployType().toLowerCase());  //the deploy type matches the playbook file name
  }

  /**
   * Starts the process that deploys an environment.
   */
  private int runDeployEnv(AbstractBuild<?,?> build, BuildListener listener, Launcher launcher)
    throws IOException, InterruptedException {
    final Configuration freemarkerConf = getFreemarkerConf("/ansible");
    final Map<String, Object> data = buildTemplateModel(build);
    final File hostsFile = runTemplate(freemarkerConf, data, "deploy_hosts", "");
    return startProcess(DEPLOY_JOB_SH, listener, launcher, build, hostsFile.getAbsolutePath(), String.format(
      ALL_SERVICES_FMT,
      environment.name().toLowerCase()),deployOption.getDeployType().toLowerCase());
  }

  /**
   * Starts the execution of deployment script using the listed parameters.
   */
  private int startProcess(
    final String scriptFile,
    final BuildListener listener,
    final Launcher launcher,
    final AbstractBuild<?,?> build,
    final String... params
  ) throws IOException, InterruptedException {

    if (Strings.isNullOrEmpty(((DeployDescriptor) getDescriptor()).getCredentialsId())) { //Plugin hasn't been configured yet
      listener.getLogger()
        .println("Git credentials hasn't been defined, please do it in the main Jenkins configuration page");
      throw new IllegalStateException();
    }

    final StandardUsernamePasswordCredentials credentials = lookupGitCredentials();

    final FilePath ansibleScriptFile = new FilePath(new File(DeployBuilder.class.getResource(scriptFile).getFile()));

    /**
     * Order of parameters in deploy.sh script: environment, hosts file, services file and buildId.
     * Git credentials are passed in a single string: username:password.
     */

    // Executes the script file on Jenkins server/slave


    return ansibleScriptFile.act(new FilePath.FileCallable<Integer>() {
      @Override
      public void checkRoles(RoleChecker roleChecker) throws SecurityException {
        return;
      }

      public Integer invoke(File f, VirtualChannel channel)
                                     throws IOException, InterruptedException {
                                      //A copy of the bash script is required because it's inside a jar file
                                     try (InputStream inScript = DeployBuilder.class.getResourceAsStream(scriptFile)) {
                                       File localScript = new File(build.getRootDir(), f.getName());
                                       Files.copy(inScript, localScript.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                       localScript.setExecutable(true, false);
                                       ArgumentListBuilder argumentBuilder = new ArgumentListBuilder();
                                       argumentBuilder
                                         .add(localScript.getPath())
                                         .add(credentials.getUsername() + ':' + credentials.getPassword(), true)
                                         .add(environment.name().toLowerCase())
                                         .add(params)
                                         .add(build.getId())
                                         .add(cdeployBranch);
                                       return launcher.launch()
                                         .cmds(argumentBuilder)
                                         .stdout(listener).pwd(build.getWorkspace()).join(); //adding a mask to the credentials argument
                                     }
                                   }
                                 }

    );
  }

  /**
   * Lookup the Git credentials.
   */
  private StandardUsernamePasswordCredentials lookupGitCredentials() {
    return CredentialsMatchers.firstOrNull(CredentialsProvider.lookupCredentials(StandardUsernamePasswordCredentials.class,
                                                                                 Jenkins.getInstance(),
                                                                                 ACL.SYSTEM,
                                                                                 Collections.<DomainRequirement>emptyList()),
                                           CredentialsMatchers.withId(((DeployDescriptor) getDescriptor()).getCredentialsId()));
  }

  /**
   * Checks if all the artifacts must be deployed onto the target environment.
   */
  private boolean deployAll() {
    return deployOption != null && deployOption.getOptionalDeployArtifact() == null;
  }

  public Environment getEnvironment() {
    return environment;
  }

  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  public DeployOption getDeployOption() {
    return deployOption;
  }

  public String getCdeployBranch() {
    return cdeployBranch;
  }

  public void setCdeployBranch(String cdeployBranch) {
    this.cdeployBranch = cdeployBranch;
  }

  /**
   * Plugin descriptor: only provides a name and the ListBoxModels for selection lists.
   */
  @Extension
  public static final class DeployDescriptor extends BuildStepDescriptor<Publisher> {

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
