package org.gbif.deployplugin;

import java.util.Iterator;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import hudson.util.ListBoxModel;

/**
 * Represents a maven artifact that can be deployed by automation.
 */
public class Artifact {

  private static final String LATEST_VERSION = "LATEST";
  private static final Character FULL_NAME_SEPARATOR = '/';
  private static final Joiner NAME_JOINER = Joiner.on(FULL_NAME_SEPARATOR);
  private static final Splitter NAME_SPLITTER = Splitter.on(FULL_NAME_SEPARATOR);

  //List of GBIF artifacts that can be deployed by this plugin.
  public static List<Artifact> DEPLOY_ARTIFACTS =
    new ImmutableList.Builder<Artifact>().add(new Artifact("org.gbif.occurrence", "occurrence-ws"))
      .add(new Artifact("org.gbif.registry", "registry-ws"))
      .add(new Artifact("org.gbif.checklistbank", "checklistbank-ws"))
      .add(new Artifact("org.gbif.crawler", "crawler-ws"))
      .add(new Artifact("org.gbif.metrics", "metrics-ws"))
      .add(new Artifact("org.gbif", "tile-server"))
      .add(new Artifact("org.gbif", "image-cache"))
      .add(new Artifact("org.gbif.geocode", "geocode-ws"))
      .build();

  //Used to display selection lists in the UI.
  public static ListBoxModel LIST_BOX_MODEL = initListBoxModel();

  /**
   * Loads the ListBoxModel.
   */
  private static ListBoxModel initListBoxModel() {
    ListBoxModel items = new ListBoxModel();
    for (Artifact artifact : DEPLOY_ARTIFACTS) {
      items.add(artifact.getArtifactId(), artifact.toFullName());
    }
    return items;
  }

  private final String groupId;
  private final String artifactId;
  private final String version;

  /**
   * Full constructor.
   */
  public Artifact(String groupId, String artifactId, String version) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
  }

  /**
   * This constructor uses the default version 'LATEST'.
   */
  public Artifact(String groupId, String artifactId) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    version = LATEST_VERSION;
  }

  /**
   * Maven groupId.
   */
  public String getGroupId() {
    return groupId;
  }

  /**
   * Maven artifactId.
   */
  public String getArtifactId() {
    return artifactId;
  }

  public String getVersion() {
    return version;
  }

  /**
   * Returns the full name of this artifact: groupId/artifactId/version.
   */
  public String toFullName() {
    return NAME_JOINER.join(groupId, artifactId, version);
  }

  /**
   * Creates an artifact instance from a String with the pattern: groupId/artifactId/version.
   */
  public static Artifact fromFullName(String fullName) {
    Iterator<String> fullNameIt = NAME_SPLITTER.split(fullName).iterator();
    return new Artifact(fullNameIt.next(), fullNameIt.next(), fullNameIt.next());
  }
}
