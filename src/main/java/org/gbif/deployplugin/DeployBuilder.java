package org.gbif.deployplugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Closer;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.yaml.snakeyaml.JavaBeanLoader;

/**
 * Sample {@link Builder}.
 * <p>
 * When the user configures the project and enables this builder, {@link DescriptorImpl#newInstance(StaplerRequest)} is
 * invoked and a new {@link DeployBuilder} is created. The created instance is persisted to the project configuration
 * XML by using XStream, so this allows you to use instance fields (like {@link #name}) to remember the configuration.
 * <p>
 * When a build is performed, the {@link #perform(AbstractBuild, Launcher, BuildListener)} method will be invoked.
 * 
 * @author Kohsuke Kawaguchi
 */
public class DeployBuilder extends Builder {

  private List<Artifact> artifacts;
  private static final String DEFAULT_PACKAGING = "jar";
  private static final String DEFAULT_VERSION = "LATEST";

  private Environment environment = Environment.DEV;

  // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
  @DataBoundConstructor
  public DeployBuilder(List<Artifact> artifacts, String environment) {
    this.artifacts = artifacts;
    this.environment = Environment.valueOf(environment);
  }


  public List<Artifact> getArtifacts() {
    return artifacts;
  }

  public void setArtifacts(List<Artifact> artifacts) {
    this.artifacts = artifacts;
  }

  public void buildAnsibleTemplates(BuildListener listener) throws IOException {
    Closer closer = Closer.create();
    try {
      freemarker.template.Configuration configuration = new freemarker.template.Configuration();
      configuration.setClassForTemplateLoading(DeployBuilder.class, "/ansible");
      Template varsTemplate = configuration.getTemplate("variables.yaml");
      Template hostsTemplate = configuration.getTemplate("hosts");
      Template siteTemplate = configuration.getTemplate("site.yaml");
      Map<String, Object> data = new HashMap<String, Object>();
      data.put("nexusHostname", getDescriptor().getNexusHostname());
      data.put("environment", getDescriptor().getConfiguration().getEnvironmentConfiguration(getEnvironment()));
      data.put("artifacts", getArtifacts());
      File varsFile = File.createTempFile("variables", ".yaml");
      File hostsFile = File.createTempFile("hosts", "");
      File siteFile = File.createTempFile("site", ".yaml");
      Writer varsOut = closer.register(new FileWriter(varsFile));
      Writer hostOut = closer.register(new FileWriter(hostsFile));
      Writer siteOut = closer.register(new FileWriter(siteFile));
      varsTemplate.process(data, varsOut);
      hostsTemplate.process(data, hostOut);
      siteTemplate.process(data, siteOut);
      File ansibleScriptFile = new File(DeployBuilder.class.getResource("/bin/runAnsiblePlaybook.sh").getFile());
      ansibleScriptFile.setExecutable(true, false);
      Process process =
        new ProcessBuilder(ansibleScriptFile.getAbsolutePath(),
          hostsFile.getAbsolutePath(), varsFile.getAbsolutePath()).start();
      int exitValue = process.waitFor();
      BufferedReader buf = closer.register(new BufferedReader(new InputStreamReader(process.getInputStream())));
      BufferedReader errorBuf = closer.register(new BufferedReader(new InputStreamReader(process.getErrorStream())));
      String line = "";
      while ((line = buf.readLine()) != null) {
        listener.getLogger().println(line);
      }
      while ((line = errorBuf.readLine()) != null) {
        listener.getLogger().println(line);
      }
    } catch (IOException e) {
      Throwables.propagate(e);
    } catch (TemplateException e) {
      Throwables.propagate(e);
    } catch (InterruptedException e) {
      Throwables.propagate(e);
    } finally {
      closer.close();
    }
  }


  public void runAnsibleScript(String varsFile, String hostsFile, String siteFile) {


  }

  @Override
  public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
    try {
      buildAnsibleTemplates(listener);
      listener.getLogger().println("Running!");
      return true;
    } catch (IOException e) {
      listener.getLogger().println("Error generating ansible templates");
      listener.getLogger().println(e);
      return false;
    }
  }

  // Overridden for better type safety.
  // If your plugin doesn't really define any property on Descriptor,
  // you don't have to do this.
  @Override
  public DescriptorImpl getDescriptor() {
    return (DescriptorImpl) super.getDescriptor();
  }


  public Environment getEnvironment() {
    return environment;
  }


  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  /**
   * Descriptor for {@link DeployBuilder}. Used as a singleton.
   * The class is marked as public so that it can be accessed from views.
   * <p>
   * See <tt>src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt> for the actual HTML fragment
   * for the configuration screen.
   */
  @Extension
  // This indicates to Jenkins that this is an implementation of an extension point.
  public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

    private Configuration configuration;

    private final JavaBeanLoader<Configuration> javaBeanLoader = new JavaBeanLoader<Configuration>(Configuration.class);

    private static final String CONF_FILE = "/artifacts.yaml";

    private String nexusHostname = "repository.gbif.org";

    private RepoClient repoClient;


    /**
     * In order to load the persisted global configuration, you have to
     * call load() in the constructor.
     */
    public DescriptorImpl() {
      configuration = javaBeanLoader.load(DeployBuilder.class.getResourceAsStream(CONF_FILE));
      load();
    }

    public RepoClient repoClient() {
      if (repoClient == null) {
        repoClient = new RepoClient(nexusHostname);
      }
      return repoClient;
    }


    public List<Artifact> getVersions(String groupId, String artifactId, String environment) {
      EnvironmentConfiguration environmentConfiguration =
        configuration.getEnvironmentConfiguration(Environment.valueOf(environment));
      NexusSearchResults results =
        repoClient().search(groupId, artifactId, "", environmentConfiguration.getRepositoryId(),
          DEFAULT_PACKAGING);
      return new ImmutableList.Builder<Artifact>().add(new Artifact(groupId, artifactId, DEFAULT_VERSION))
        .addAll(results.getData()).build();
    }

    /**
     * Performs on-the-fly validation of the form field 'nexusHostname'.
     * 
     * @param value
     *        This parameter receives the value that the user has typed.
     * @return
     *         Indicates the outcome of the validation. This is sent to the browser.
     *         <p>
     *         Note that returning {@link FormValidation#error(String)} does not prevent the form from being saved. It
     *         just means that a message will be displayed to the user.
     */
    public FormValidation doCheckNexusHostname(@QueryParameter String value)
      throws IOException, ServletException {
      if (value.length() == 0)
        return FormValidation.error("Please set a name");
      try {
        InetAddress.getByName(value);
      } catch (Exception e) {
        return FormValidation.warning("Hostname is invalid");
      }
      return FormValidation.ok();
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
      // To persist global configuration information,
      // set that to properties and call save().

      // ^Can also use req.bindJSON(this, formData);
      // (easier when there are many fields; need set* methods for this, like setUseFrench)
      nexusHostname = formData.getString("nexusHostname");
      save();
      return super.configure(req, formData);
    }

    public Configuration getConfiguration() {
      return configuration;
    }


    public void setConfiguration(Configuration configuration) {
      this.configuration = configuration;
    }


    public String getNexusHostname() {
      return nexusHostname;
    }


    public void setNexusHostname(String nexusHostname) {
      this.nexusHostname = nexusHostname;
    }


  }
}
