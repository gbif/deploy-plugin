package org.gbif.deployplugin.model;

/**
 * Represents a maven artifact that can be deployed by automation.
 */
public class Service {

  private String groupId;
  private String artifactId;
  private String version;
  private String httpPort;
  private String httpAdminPort;
  private String testOnDeploy;

  /**
   * Maven groupId.
   */
  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  /**
   * Maven artifactId.
   */
  public String getArtifactId() {
    return artifactId;
  }

  public void setArtifactId(String artifactId) {
    this.artifactId = artifactId;
  }

  /**
   * Artifact version.
   */
  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * Indicates if this artifact has to be tested right after being deployed.
   */
  public String getTestOnDeploy() {
    return testOnDeploy;
  }

  public void setTestOnDeploy(String testOnDeploy) {
    this.testOnDeploy = testOnDeploy;
  }

  /**
   * Public http port number that the service will attempt to use.
   */
  public String getHttpPort() {
    return httpPort;
  }

  public void setHttpPort(String httpPort) {
    this.httpPort = httpPort;
  }

  /**
   *  Public http admin port number that the service will attempt to use.
   */
  public String getHttpAdminPort() {
    return httpAdminPort;
  }

  public void setHttpAdminPort(String httpAdminPort) {
    this.httpAdminPort = httpAdminPort;
  }

}
