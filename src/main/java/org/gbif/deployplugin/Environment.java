package org.gbif.deployplugin;

import hudson.util.ListBoxModel;

/**
 * Deployment environments.
 */
public enum Environment {

  DEV, UAT, PROD;

  //Used to display selection lists in the UI
  public static ListBoxModel LIST_BOX_MODEL = initListBoxModel();

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
