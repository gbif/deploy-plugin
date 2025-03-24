package org.gbif.deployplugin.model;

import java.util.List;

/**
 * This class represents the configuration settings of a GBIF execution environment, it was created for the convenience
 * of loading the service.yml file into a Java class.
 *
 */
public class ConfigurationEnvironment {

    private List<Service> services;

    /**
     * List of services running on this environment.
     */
    public List<Service> getServices() {
      return services;
    }

    public void setServices(List<Service> services) {
      this.services = services;
    }
  }