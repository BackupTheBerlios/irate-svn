package irate.swt.plugin.unratednotifier;

/**
 * Date Created: Feb 22, 2004
 * Date Updated: $Date: 2004/02/22 19:46:28 $
 * @author Creator: Mathieu Mallet
 * @author Updated: $Author: emh_mark3 $
 * @version $Revision: 1.1 $ */

public class Resources {
  /** Get a resource string from the properties file associated with this 
   * package.
   */
  public static String getString(String key) {
    return irate.resources.BaseResources.getString("irate.swt.plugin.unratednotifier.locale", key);
  }
}
