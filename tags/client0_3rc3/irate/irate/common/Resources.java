/*
 * Created on Dec 4, 2003
 */
package irate.common;

/**
 * @author Anthony Jones
 */
public class Resources {
  /** Get a resource string from the properties file associated with this 
   * package.
   */
  public static String getString(String key) {
    return irate.resources.BaseResources.getString("irate.common.locale", key);
  }
}
