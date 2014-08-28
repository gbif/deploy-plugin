package org.gbif.deployplugin;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;


public class Artifact extends AbstractDescribableImpl<Artifact> {

  private String groupId;
  private String artifactId;
  private String title;
  private String description;
  private String version;

  public Artifact() {

  }

  public Artifact(String groupId, String artifactId, String title, String description, String version) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.title = title;
    this.description = description;
    this.version = version;
  }

  @DataBoundConstructor
  public Artifact(String groupId, String artifactId, String version) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public void setArtifactId(String artifactId) {
    this.artifactId = artifactId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }


  public String getVersion() {
    return version;
  }


  public void setVersion(String version) {
    this.version = version;
  }


  @Extension
  public static class DescriptorImpl extends Descriptor<Artifact> {

    public String getDisplayName() {
      return "";
    }
  }
}
