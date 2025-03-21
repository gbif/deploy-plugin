package org.gbif.deployplugin.model;

/**
 * Represents a maven artifact that can be deployed by automation.
 */
public class Service {

  private String groupId;
  private String artifactId;
  private String packaging;
  private String classifier;
  private String version;
  private String framework;
  private String instanceName;
  private String httpPort;
  private String httpAdminPort;
  private String testOnDeploy;
  private String useFixedPorts;
  private String maxConnections;

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
   * Maven artifact classifier.
   */
  public String getClassifier() {
    return classifier;
  }

  public void setClassifier(String classifier) {
    this.classifier = classifier;
  }

  /**
   * Maven packaging
   * @return
   */
  public String getPackaging() {
    return packaging;
  }

  public void setPackaging(String packaging) {
    this.packaging = packaging;
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
   * Artifact framework
   */
  public String getFramework() {
    return framework;
  }

  public void setFramework(String framework) {
    this.framework = framework;
  }

  public String getInstanceName() {
    return instanceName;
  }

  public void setInstanceName(String instanceName) {
    this.instanceName = instanceName;
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

  /**
   * Does this service require fixed ports.
   */
  public String getUseFixedPorts() {
    return useFixedPorts;
  }

  public void setUseFixedPorts(String useFixedPorts) {
    this.useFixedPorts = useFixedPorts;
  }

  /**
   * Maximum number of backend connections.
   */
  public String getMaxConnections() {
    return maxConnections;
  }

  public void setMaxConnections(String maxConnections) {
    this.maxConnections = maxConnections;
  }

}
