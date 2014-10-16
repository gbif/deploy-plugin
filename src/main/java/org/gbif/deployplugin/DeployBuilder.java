package org.gbif.deployplugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.util.Map;
import java.util.Scanner;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.io.Closer;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Jenkins BuildStep pluging that triggers the deployment of artifact.
 */
public class DeployBuilder extends Builder {

  private Environment environment = Environment.DEV;

  private String groupId;

  private String artifactId;

  private static final String ERROR_MSG = "Error executing deployment scripts";

  // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
  @DataBoundConstructor
  public DeployBuilder(String environment, String groupId, String artifactId) {
    this.environment = Environment.valueOf(environment);
    this.artifactId = artifactId;
    this.groupId = groupId;
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
    freemarker.template.Configuration freemarkerConf,
    Map<String, Object> data,
    String fileName,
    String fileExtension
  ) {
    Closer closer = Closer.create();
    try {
      final Template template = freemarkerConf.getTemplate(fileName + (Strings.isNullOrEmpty(fileExtension)? "" : "." + fileExtension));
      final File tmpFile = File.createTempFile(fileName, fileExtension);
      final Writer varsOut = closer.register(new FileWriter(tmpFile));
      template.process(data, varsOut);
      return tmpFile;
    } catch (IOException ex) {
      Throwables.propagate(ex);
    } catch (TemplateException ex) {
      Throwables.propagate(ex);
    }
    throw new IllegalStateException("Error creating a template");
  }

  /**
   * Creates the data required by the freemarker templates.
   */
  private Map<String, Object> buildTemplateModel() {
    Map<String, Object> data = Maps.newHashMap();
    data.put("configuration", getDescriptor().getConfiguration(environment));
    data.put("artifactId", artifactId);
    data.put("groupId", groupId);
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
      freemarker.template.Configuration freemarkerConf = getFreemarkerConf("/ansible");
      Map<String, Object> data = buildTemplateModel();

      //Executes the template engine to create the host and variables files
      File varsFile = runTemplate(freemarkerConf, data, "variables", "yaml");
      File hostsFile = runTemplate(freemarkerConf, data, "hosts", "");

      File ansibleScriptFile = new File(DeployBuilder.class.getResource("/bin/runAnsiblePlaybook.sh").getFile());
      //sets execution permissions
      ansibleScriptFile.setExecutable(true, false);

      //Executes the ansible scripts
      Process process =
        new ProcessBuilder(ansibleScriptFile.getAbsolutePath(), hostsFile.getAbsolutePath(), varsFile.getAbsolutePath())
          .start();

      //Redirects the output and error streams
      inheritIO(process.getInputStream(), listener.getLogger());
      inheritIO(process.getErrorStream(), listener.getLogger());
      return process.waitFor() == 0;
    } catch (IOException e) {
      logAndPropagate(listener, e);
    } catch (InterruptedException e) {
      logAndPropagate(listener, e);
    }
    return false;
  }

  // Overridden for better type safety.
  // If your plugin doesn't really define any property on Descriptor,
  // you don't have to do this.
  @Override
  public DeployDescriptor getDescriptor() {
    return (DeployDescriptor) super.getDescriptor();
  }

  public Environment getEnvironment() {
    return environment;
  }

  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public void setArtifactId(String artifactId) {
    this.artifactId = artifactId;
  }

  /**
   * Descriptor for {@link DeployBuilder}. Used as a singleton.
   * The class is marked as public so that it can be accessed from views.
   * Contains the information about the deployment environments {@link org.gbif.deployplugin.EnvironmentConfiguration}.
   */
  @Extension
  // This indicates to Jenkins that this is an implementation of an extension point.
  public static final class DeployDescriptor extends BuildStepDescriptor<Builder> {

    private EnvironmentConfiguration devConfiguration;

    /**
     * In order to load the persisted global configuration, you have to
     * call load() in the constructor.
     */
    public DeployDescriptor() {
      load();
      devConfiguration = new EnvironmentConfiguration();
    }

    public boolean isApplicable(Class<? extends AbstractProject> aClass) {
      // Indicates that this builder can be used with all kinds of project types
      return true;
    }

    /**
     * This human readable name is used in the configuration screen.
     */
    public String getDisplayName() {
      return "Gbif Deployment";
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
      // Gets the dev configuration
      devConfiguration = fromForData(formData, Environment.DEV);
      save();
      return super.configure(req, formData);
    }

    /**
     * Retrieves the configuration object from the formData.
     * The env parameter is used as a prefix to get the values of a specific environment.
     */
    private static EnvironmentConfiguration fromForData(JSONObject formData, Environment env) {
      EnvironmentConfiguration configuration = new EnvironmentConfiguration();
      configuration.setEnvironmentId(env);
      configuration.setAppsServerHostname(formData.getString(env.name() + "-appsServerHostname"));
      configuration.setNexusRepositoryHost(formData.getString(env.name() + "-nexusRepositoryHost"));
      configuration.setNexusRepositoryId(formData.getString(env.name() + "-nexusRepositoryId"));
      configuration.setVarnishHost(formData.getString(env.name() + "-varnishHost"));
      configuration.setVarnishAdminPort(formData.getInt(env.name() + "-varnishAdminPort"));
      configuration.setZkHost(formData.getString(env.name() + "-zkHost"));
      return configuration;
    }

    /**
     * Gets the development configuration.
     */
    public EnvironmentConfiguration getDevConfiguration() {
      return devConfiguration;
    }

    /**
     * Return the configurations associated to the env parameter.
     */
    public EnvironmentConfiguration getConfiguration(Environment env) {
      if (Environment.DEV == env) {
        return devConfiguration;
      }
      return null;
    }

  }
}
