package org.gbif.deployplugin;


public class EnvironmentConfiguration {

  private Environment environmentId;

  private String controlHost;

  private String varnishHost;

  private String varnishAdminPort;

  private int controlHostPort;

  private String repositoryId;

  private String zkHost;

  private String stopSecret;


  public Environment getEnvironmentId() {
    return environmentId;
  }


  public void setEnvironmentId(Environment environmentId) {
    this.environmentId = environmentId;
  }


  public String getControlHost() {
    return controlHost;
  }


  public void setControlHost(String controlHost) {
    this.controlHost = controlHost;
  }


  public String getVarnishHost() {
    return varnishHost;
  }


  public void setVarnishHost(String varnishHost) {
    this.varnishHost = varnishHost;
  }


  public String getVarnishAdminPort() {
    return varnishAdminPort;
  }


  public void setVarnishAdminPort(String varnishAdminPort) {
    this.varnishAdminPort = varnishAdminPort;
  }


  public String getRepositoryId() {
    return repositoryId;
  }


  public void setRepositoryId(String repositoryId) {
    this.repositoryId = repositoryId;
  }


  public int getControlHostPort() {
    return controlHostPort;
  }


  public void setControlHostPort(int controlHostPort) {
    this.controlHostPort = controlHostPort;
  }


  public String getZkHost() {
    return zkHost;
  }


  public void setZkHost(String zkHost) {
    this.zkHost = zkHost;
  }


  public String getStopSecret() {
    return stopSecret;
  }


  public void setStopSecret(String stopSecret) {
    this.stopSecret = stopSecret;
  }


}
