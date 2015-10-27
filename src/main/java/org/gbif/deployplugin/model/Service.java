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
   * Default constructor.
   */
  public Service() {
  }

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

  public String getHttpPort() {
    return httpPort;
  }

  public void setHttpPort(String httpPort) {
    this.httpPort = httpPort;
  }

  public String getHttpAdminPort() {
    return httpAdminPort;
  }

  public void setHttpAdminPort(String httpAdminPort) {
    this.httpAdminPort = httpAdminPort;
  }

}
