/*
 * Created on Dec 5, 2003
 */
package irate.download;

import irate.resources.BaseResources;

/**
 * @author Anthony Jones
 */
public class Resources {

  private static final String LOCALE_RESOURCE_LOCATION = "irate.download.locale";

  /**
   * Get a resource string from the properties file associated with this
   * package.
   */
  public static String getString(String key) {
    return BaseResources.getString(LOCALE_RESOURCE_LOCATION, key);
  }

}
