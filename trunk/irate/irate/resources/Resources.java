/*
 * Created on Nov 13, 2003
 */
package irate.resources;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author Anthony Jones
 */
public class Resources {
  
  private static Class cls = new Resources().getClass();
  private static Hashtable cachedBundles = new Hashtable();

  public static URL getResource(String name) {
    return cls.getResource(name);
  }
  
  public static InputStream getResourceAsStream(String name) throws IOException {
    if(getResource(name) != null)
      return getResource(name).openStream();
    else
      throw new IOException();
  }
  
  
  public static String getString(String bundleName, String key) {
    try {
      if(cachedBundles.containsKey(bundleName)) {
        ResourceBundle bundle = (ResourceBundle)cachedBundles.get(bundleName);
        return bundle.getString(key); 
      }
      else {
        ResourceBundle bundle = ResourceBundle.getBundle(bundleName);
        cachedBundles.put(bundleName, bundle);
        return bundle.getString(key);
      }
    }
    catch (MissingResourceException e) {
      return '!' + key + '!';
    }
  }
  
}
