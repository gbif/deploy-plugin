package org.gbif.deployplugin;

import java.util.List;


public class Configuration {

  private List<Artifact> artifacts;

  private List<EnvironmentConfiguration> environments;

  public List<Artifact> getArtifacts() {
    return artifacts;
  }


  public void setArtifacts(List<Artifact> artifacts) {
    this.artifacts = artifacts;
  }


  public List<EnvironmentConfiguration> getEnvironments() {
    return environments;
  }


  public void setEnvironments(List<EnvironmentConfiguration> environments) {
    this.environments = environments;
  }


  public EnvironmentConfiguration getEnvironmentConfiguration(Environment environment) {
    for (EnvironmentConfiguration environmentConfiguration : environments) {
      if (environmentConfiguration.getEnvironmentId() == environment) {
        return environmentConfiguration;
      }
    }
    return null;
  }

}
