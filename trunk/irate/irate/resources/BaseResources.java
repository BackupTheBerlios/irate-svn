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
import java.util.Vector;
import java.io.File;
import java.util.Enumeration;
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
   * Function used to extract a ressource into a temp folder and return the path of the
   * file. A vector can be passed to store filenames in -- those filenames refer to ressources
   * that have already been extracted (and can be reused for extra efficiency).
   */
  public static File getResourceAsFile(String name, Vector tempFiles) throws IOException {
    File myTempFile;
    String[] myStrings;   // Used in tempFiles: first string is the ressource name, second is the file path
    if(getResource(name) != null) {
      
      // Search tempFile list to see if ressource already extracted
      if (tempFiles != null) {
        for (Enumeration e = tempFiles.elements(); e.hasMoreElements();) {
          myStrings = (String[])(e.nextElement());
          if (name.equals(myStrings[0]))
            return new File(myStrings[1]);
        }
      }
      
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
      
      // Add file to filelist if required
      if (tempFiles != null) {
        myStrings = new String[] {name, myTempFile.getPath() };
        tempFiles.add(myStrings);
      }
      
      return myTempFile;
    }
    else
      throw new IOException();
  }
  
  /**
   * Get a ressource as a temporary file. A new file will be created each time
   * this function is called, even if the same ressource is accessed twice. To prevent
   * this, use getResourceAsFile(String, Vector) instead.
   */
  public static File getResourceAsFile(String name) throws IOException {
    return getResourceAsFile(name, null);
  }
  
}
