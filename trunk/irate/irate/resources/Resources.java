/*
 * Created on Nov 13, 2003
 */
package irate.resources;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author Anthony Jones
 */
public class Resources {
  
  private static Class cls = new Resources().getClass();
  private static final String BUNDLE_NAME = "irate.resources.irate"; 
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

  public static URL getResource(String name) {
    return cls.getResource(name);
  }
  
  public static InputStream getResourceAsStream(String name) throws IOException {
    if(getResource(name) != null)
      return getResource(name).openStream();
    else
      throw new IOException();
  }
  
  public static String getString(String key) {
    try {
      return RESOURCE_BUNDLE.getString(key);
    }
    catch (MissingResourceException e) {
      return '!' + key + '!';
    }
  }
  
}
