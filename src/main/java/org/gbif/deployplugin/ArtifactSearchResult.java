package org.gbif.deployplugin;



public class ArtifactSearchResult {

  private String resourceURI;
  private String groupId;
  private String artifactId;
  private String version;
  private String classifier;
  private String packaging;
  private String extension;
  private String repoId;
  private String contextId;
  private String pomLink;
  private String artifactLink;

  public String getResourceURI() {
    return resourceURI;
  }

  public void setResourceURI(String resourceURI) {
    this.resourceURI = resourceURI;
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

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getClassifier() {
    return classifier;
  }

  public void setClassifier(String classifier) {
    this.classifier = classifier;
  }

  public String getPackaging() {
    return packaging;
  }

  public void setPackaging(String packaging) {
    this.packaging = packaging;
  }

  public String getExtension() {
    return extension;
  }

  public void setExtension(String extension) {
    this.extension = extension;
  }

  public String getRepoId() {
    return repoId;
  }

  public void setRepoId(String repoId) {
    this.repoId = repoId;
  }

  public String getContextId() {
    return contextId;
  }

  public void setContextId(String contextId) {
    this.contextId = contextId;
  }

  public String getPomLink() {
    return pomLink;
  }

  public void setPomLink(String pomLink) {
    this.pomLink = pomLink;
  }

  public String getArtifactLink() {
    return artifactLink;
  }

  public void setArtifactLink(String artifactLink) {
    this.artifactLink = artifactLink;
  }


}
