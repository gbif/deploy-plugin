package org.gbif.deployplugin.model;

import java.util.List;

public class ConfigurationEnvironment {


  private List<Service> services;

  public List<Service> getServices() {
    return services;
  }

  public void setServices(List<Service> services) {
    this.services = services;
  }
}
