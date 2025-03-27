package org.gbif.jenkins.deploy;

import hudson.util.ListBoxModel;

/**
 * Deployment environments.
 */
public enum Environment {

  DEV, UAT, PROD, DEV2, UAT2, PROD2;

  //Used to display selection lists in the UI
  public static final ListBoxModel LIST_BOX_MODEL = initListBoxModel();

  /**
   * Loads the ListBoxModel.
   */
  private static ListBoxModel initListBoxModel() {
    ListBoxModel items = new ListBoxModel();
    for (Environment env : Environment.values()) {
      items.add(env.name(), env.name());
    }
    return items;
  }
}
