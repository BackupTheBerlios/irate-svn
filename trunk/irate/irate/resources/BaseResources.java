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
import java.io.File;
import java.net.URLConnection;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;

/**
 * @author Anthony Jones
 */
public class BaseResources {
  
  private static Class cls = new BaseResources().getClass();
  private static Hashtable cachedBundles = new Hashtable();
  private static Hashtable temporaryFiles = new Hashtable();

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
  
  /**
   * Function used to extract a resource into a temp folder and return the path of the
   * file. If the resource has already been extracted, a File referencing the existing file is
   * returned.
   */
  public static File getResourceAsFile(String name) throws IOException {
    File myTempFile;
    Object existingFile;
    
    // Check if resource has already been extracted
    if ((existingFile = temporaryFiles.get(name)) != null)
      return (File)existingFile;
    
    if(getResource(name) != null) {
      // Create temp file
      myTempFile = File.createTempFile("irate", name);
      myTempFile.deleteOnExit();
      
      // Open ressource
      URL url = getResource(name);
      URLConnection urlc = url.openConnection();
      InputStream is = url.openStream();
      BufferedReader in = new BufferedReader(new InputStreamReader(is));
      BufferedWriter out = new BufferedWriter(new FileWriter(myTempFile));
      
      // Copy resource into temp file
      int onechar;
      while ((onechar = in.read()) != -1)
        out.write(onechar);
      
      // Close streams
      in.close();
      out.close();
      
      // Add file to filelist
      temporaryFiles.put(name, myTempFile);
      
      return myTempFile;
    }
    else
      throw new IOException();
  }
  
}
