package org.gbif.deployplugin;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import com.google.common.base.Strings;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * Client to the Nexus repository.
 */
public class RepoClient {

  private final Client client;
  private final WebResource searchResource;

  // Path to the search service
  private static final String RESOUCER_PATH_FMT = "http://%s/service/local/lucene/search";

  /**
   * Creates a MultivaluedMap that contains the parameters to perform a artifact search.
   */
  private MultivaluedMap<String, String> buildSearchParameters(String groupId, String artifactId, String version,
    String repositoryId,
    String packaging) {
    MultivaluedMap<String, String> params = new MultivaluedMapImpl();
    if (!Strings.isNullOrEmpty(groupId)) {
      params.putSingle("g", groupId);
    }
    if (!Strings.isNullOrEmpty(artifactId)) {
      params.putSingle("a", artifactId);
    }
    if (!Strings.isNullOrEmpty(version)) {
      params.putSingle("v", version);
    }
    if (!Strings.isNullOrEmpty(repositoryId)) {
      params.putSingle("repositoryId", repositoryId);
    }
    if (!Strings.isNullOrEmpty(packaging)) {
      params.putSingle("p", packaging);
    }
    return params;
  }

  /**
   * Default constructor.
   * 
   * @param repositoryHostname this must be a host name and not HTTP URL.
   */
  public RepoClient(String repositoryHostname) {
    client = Client.create();
    searchResource = client.resource(String.format(RESOUCER_PATH_FMT, repositoryHostname));
    searchResource.accept(MediaType.APPLICATION_XML);
  }


  /**
   * Close the client and cleans up associated resources.
   */
  public void close() {
    client.destroy();
  }

  /**
   * Search artifacts. All the parameters are optional, however can't contain 'null' values.
   */
  public NexusSearchResults search(String groupId, String artifactId, String version, String repositoryId,
    String packaging) {
    return searchResource.queryParams(buildSearchParameters(groupId, artifactId, version, repositoryId, packaging))
      .accept(MediaType.APPLICATION_XML)
      .get(NexusSearchResults.class);
  }
}
