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
    new ImmutableList.Builder<Artifact>()
      .add(new Artifact("org.gbif.checklistbank", "checklistbank-nub-ws", "gbif-ws", false)) //don't test it after deploy it
      .add(new Artifact("org.gbif.checklistbank", "checklistbank-ws", "gbif-ws"))
      .add(new Artifact("org.gbif", "content-ws", "dropwizard"))
      .add(new Artifact("org.gbif.crawler", "crawler-ws", "gbif-ws"))
      .add(new Artifact("org.gbif.data", "data-repo-ws", "dropwizard"))
      .add(new Artifact("org.gbif.dataone", "dataone-membernode", "dropwizard"))
      .add(new Artifact("org.gbif.directory", "directory-ws", "gbif-ws"))
      .add(new Artifact("org.gbif.geocode", "geocode-ws", "gbif-ws"))
      .add(new Artifact("org.gbif.maps", "mapnik-server", "tar.gz", LATEST_VERSION, "nodejs", false))
      .add(new Artifact("org.gbif.metrics", "metrics-ws", "gbif-ws"))
      .add(new Artifact("org.gbif.occurrence", "occurrence-ws", "gbif-ws"))
      .add(new Artifact("org.gbif.basemaps", "raster-basemap-server", "tar.gz", LATEST_VERSION, "nodejs", false))
      .add(new Artifact("org.gbif.registry", "registry-ws", "gbif-ws"))
      .add(new Artifact("org.gbif.validator", "validator-ws", "gbif-ws"))
      .add(new Artifact("org.gbif.maps", "vectortile-server", "dropwizard"))
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
  private final String packaging;
  private final String version;
  private final String framework;
  private final boolean testOnDeploy;

  /**
   * Full constructor.
   */
  public Artifact(String groupId, String artifactId, String packaging, String version, String framework, boolean testOnDeploy) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.packaging = packaging;
    this.version = version;
    this.framework = framework;
    this.testOnDeploy = testOnDeploy;
  }

  /**
   * This constructor uses the default version 'LATEST'.
   */
  public Artifact(String groupId, String artifactId, String framework, boolean testOnDeploy) {
    this(groupId, artifactId, "jar", LATEST_VERSION, framework, testOnDeploy);
  }

  /**
   * This constructor uses the default version 'LATEST' and testOnDeploy = true.
   */
  public Artifact(String groupId, String artifactId, String framework) {
    this(groupId, artifactId, "jar", LATEST_VERSION, framework, true);
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

  public String getPackaging() {
    return packaging;
  }

  public String getVersion() {
    return version;
  }

  public String getFramework() {
    return framework;
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
