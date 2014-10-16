package org.gbif.deployplugin;

public class EnvironmentConfiguration {

  private Environment environmentId;

  private String appsServerHostname;

  private String varnishHost;

  private Integer varnishAdminPort;

  private String nexusRepositoryId;

  private String nexusRepositoryHost;

  private String zkHost;

  public Environment getEnvironmentId() {
    return environmentId;
  }

  public void setEnvironmentId(Environment environmentId) {
    this.environmentId = environmentId;
  }

  public String getAppsServerHostname() {
    return appsServerHostname;
  }

  public void setAppsServerHostname(String appsServerHostname) {
    this.appsServerHostname = appsServerHostname;
  }

  public String getVarnishHost() {
    return varnishHost;
  }

  public void setVarnishHost(String varnishHost) {
    this.varnishHost = varnishHost;
  }

  public Integer getVarnishAdminPort() {
    return varnishAdminPort;
  }

  public void setVarnishAdminPort(Integer varnishAdminPort) {
    this.varnishAdminPort = varnishAdminPort;
  }

  public String getNexusRepositoryHost() {
    return nexusRepositoryHost;
  }

  public void setNexusRepositoryHost(String nexusRepositoryHost) {
    this.nexusRepositoryHost = nexusRepositoryHost;
  }

  public String getNexusRepositoryId() {
    return nexusRepositoryId;
  }

  public void setNexusRepositoryId(String nexusRepositoryId) {
    this.nexusRepositoryId = nexusRepositoryId;
  }

  public String getZkHost() {
    return zkHost;
  }

  public void setZkHost(String zkHost) {
    this.zkHost = zkHost;
  }

}
