package org.gbif.deployplugin;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class that represents a search result of the Nexus lucene search service.
 */
@XmlRootElement(name = "searchNGResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class NexusSearchResults {

  private int totalCount;
  private int from;
  private int count;
  private boolean tooManyResults;

  // Results are returned in the XML path data/artifact
  @XmlElementWrapper(name = "data")
  @XmlElement(name = "artifact")
  private List<Artifact> data;

  /**
   * Total count of results.
   */
  public int getTotalCount() {
    return totalCount;
  }

  public void setTotalCount(int totalCount) {
    this.totalCount = totalCount;
  }

  /**
   * Defines how many items to skip before beginning to return rows.
   */
  public int getFrom() {
    return from;
  }

  public void setFrom(int from) {
    this.from = from;
  }

  /**
   * Total of rows that are returned.
   */
  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public boolean isTooManyResults() {
    return tooManyResults;
  }

  public void setTooManyResults(boolean tooManyResults) {
    this.tooManyResults = tooManyResults;
  }

  /**
   * List of artifacts returned by the search service.
   */
  public List<Artifact> getData() {
    return data;
  }

  public void setData(List<Artifact> data) {
    this.data = data;
  }


}
