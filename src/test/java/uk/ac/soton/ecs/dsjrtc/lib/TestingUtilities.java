package uk.ac.soton.ecs.dsjrtc.lib;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * General testing utilities.
 *
 * @author David Jones (dsj1n15@soton.ac.uk)
 * @author Richard Crosland (rtc1g16@soton.ac.uk)
 */
public class TestingUtilities {

  /**
   * Convert a relative path of a resource in the project resource folder to an absolute path.
   * 
   * @param relativePath Path from resource folder root
   * @return Absolute path to resource, null if resource is not found
   */
  public static String getResourcePath(String relativePath) {
    // Find the resource as a URL
    URL datasetURL = TestingUtilities.class.getResource("/" + relativePath);
    if (datasetURL == null) {
      System.err.println(String.format("Resource not found: '%s'", relativePath));
    } else {
      try {
        // Remove any URL artifacts
        URI datasetURI = new URI(datasetURL.toString());
        return datasetURI.toString();
      } catch (URISyntaxException e) {
        e.printStackTrace();
      }
    }
    return null;
  }

}
