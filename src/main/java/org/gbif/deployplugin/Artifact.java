package org.gbif.deployplugin;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import hudson.util.ListBoxModel;

/**
 * Represents a maven artifact that can be deployed by automation.
 */
public class Artifact {

  private static final String LATEST_VERSION = "LATEST";
  private static final Character FULL_NAME_SEPARATOR = '/';
  private static final Joiner NAME_JOINER = Joiner.on(FULL_NAME_SEPARATOR);

  //List of GBIF artifacts that can be deployed by this plugin.
  public static final List<Artifact> DEPLOY_ARTIFACTS =
    new ImmutableList.Builder<Artifact>().add(new Artifact("org.gbif.occurrence", "occurrence-ws"))
      .add(new Artifact("org.gbif.registry", "registry-ws"))
      .add(new Artifact("org.gbif.checklistbank", "checklistbank-ws"))
      .add(new Artifact("org.gbif.checklistbank", "checklistbank-nub-ws", false)) //don't test it after deploy it
      .add(new Artifact("org.gbif.crawler", "crawler-ws"))
      .add(new Artifact("org.gbif.metrics", "metrics-ws"))
      .add(new Artifact("org.gbif", "tile-server"))
      .add(new Artifact("org.gbif", "image-cache"))
      .add(new Artifact("org.gbif.geocode", "geocode-ws"))
      .add(new Artifact("org.gbif.directory", "directory-ws"))
      .build();

  //Used to display selection lists in the UI.
  public static final ListBoxModel LIST_BOX_MODEL = initListBoxModel();

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
  private final boolean testOnDeploy;

  /**
   * Full constructor.
   */
  public Artifact(String groupId, String artifactId, String version, boolean testOnDeploy) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
    this.testOnDeploy = testOnDeploy;
  }

  /**
   * This constructor uses the default version 'LATEST'.
   */
  public Artifact(String groupId, String artifactId, boolean testOnDeploy) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    version = LATEST_VERSION;
    this.testOnDeploy= testOnDeploy;
  }

  /**
   * This constructor uses the default version 'LATEST' and testOnDeploy = true.
   */
  public Artifact(String groupId, String artifactId) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    version = LATEST_VERSION;
    testOnDeploy = true;
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
   * Indicates if this artifact has to be tested right after being deployed.
   */
  public boolean isTestOnDeploy() {
    return testOnDeploy;
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
  public static Artifact fromFullName(final String fullName) {
    return Iterables.find(DEPLOY_ARTIFACTS, new Predicate<Artifact>() {
      public boolean apply(@Nullable Artifact input) {
        return input.toFullName().equalsIgnoreCase(fullName);
      }
    });
  }
}
