package org.gbif.deployplugin.model;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.google.common.base.Throwables;
import com.google.common.io.Closeables;
import org.kohsuke.github.GitHub;
import org.yaml.snakeyaml.Yaml;

/**
 * Utility class to read the services.yml definition from the Git repository gbif-configuration.
 */
public class GitHubServicesReader {

  /**
   * Organization Name in GitHub.
   */
  private static final String GIT_GBIF_ORG = "gbif";

  /**
   * GBIF configuration repository.
   */
  private static final String GIT_GBIF_CONF_REPO = "gbif-configuration";

  /**
   * Path to services.yml file.
   */
  private static final String GIT_SERVICES_PATH_FMT = "/environments/%s/services.yml";

  /**
   * Private constructor.
   */
  private GitHubServicesReader(){
    //empty constructor
  }

  /**
   * Loads the list of services from YAML file.
   * @param environment lowerecased environment name to be to be loaded
   * @param gitHubCredentials user name and password to access the repository gbif-configuration
   * @return a ConfigurationEnvironment with the its list of services
   * @throws IOException if an error occurs while reading the file
   */
  public static ConfigurationEnvironment getEnvironmentServices(
    String environment,
    StandardUsernamePasswordCredentials gitHubCredentials
  ) throws IOException {
    InputStream artifactsStream = null;

    try {
      artifactsStream = GitHub.connectUsingPassword(gitHubCredentials.getUsername(),
                                                    gitHubCredentials.getPassword().getPlainText())
        .getOrganization(GIT_GBIF_ORG)
        .getRepository(GIT_GBIF_CONF_REPO)
        .getFileContent(String.format(GIT_SERVICES_PATH_FMT, environment)).read();
      return new Yaml().loadAs(artifactsStream, ConfigurationEnvironment.class);
    } catch (FileNotFoundException ex){
      throw Throwables.propagate(ex);
    } catch (IOException ex){
      throw Throwables.propagate(ex);
    } finally {
      Closeables.close(artifactsStream,false);
    }
  }
}
