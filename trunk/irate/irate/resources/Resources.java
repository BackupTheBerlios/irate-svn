/*
 * Created on Nov 13, 2003
 */
package irate.resources;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @author Anthony Jones
 */
public class Resources {
  
  private static Class cls = new Resources().getClass();

  public static URL getResource(String name) {
    return cls.getResource(name);
  }
  
  public static InputStream getResourceAsStream(String name) throws IOException {
    if(getResource(name) != null)
      return getResource(name).openStream();
    else
      throw new IOException();
  }
  
}
