package irate.plugin.marsyas;

/**
 * @author Anthony Jones
 */
public class Resources {

  /** Get a resource string from the properties file associated with this 
   * package.
   */
  public static String getString(String key) {
    return irate.resources.BaseResources.getString("irate.plugin.marsyas.locale", key);
  }

}
