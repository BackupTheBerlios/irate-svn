package irate.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Hashtable;

import nanoxml.XMLElement;


public class Preferences {

  private static File home;
  private static File config;
  private static File temp;

  static {
    File dir = getPrefsDirectory();
    config = new File(dir, "irate.xml");
    temp = new File(dir, "irate.xml~");
  }
  
  public static File getPrefsDirectory() {
    home = new File(System.getProperties().getProperty("user.home"));
    File dir = new File(home, "/irate");
    if (System.getProperty("os.name").toLowerCase().startsWith("mac os x")) {
      // If the user doesn't already have an irate dir and hasn't removed ~/Music,
      // let's put our stuff there alongside iTunes etc.
      if (!dir.exists() && new File(home, "/Music").exists()) {
        dir = new File(home, "/Music/iRATE");
      }
    }
    if (!dir.exists()) {
      dir.mkdir();
    }
    return dir;
  }
  
  /** 
   * This method accesses the irate.xml configuration file and returns a 
   * string representation of the user's preferred download directory.  That is,
   * the directory where trackdatabase.xml is stored.
   * @return the directory location of where the trackdatabase.xml file is stored
   */
  public static String getUserDownloadDirectoryPreference() {
    return getUserPreference("downloadDir");
  }
  
  public static void setPlayer(String player) {
    setUserPreference("Player", player);
  }

  public static String getPlayer() {
    return getUserPreference("Player");
  }

  public static boolean isRoboJockEnabled() {
    return isUserPreference("RoboJock");
  }

  public static void setRoboJockEnabled(boolean enabled) {
    setUserPreference("RoboJock", enabled);
  }

  public static String getUserPreference(String prefName) {
    try {    
      XMLElement docElt = getConfigFileAsXML();
  
      Enumeration e = docElt.enumerateChildren();
      while(e.hasMoreElements()) {
        XMLElement elt = (XMLElement)e.nextElement();
        if (elt.getName().equals("preference")) {
          String identifier = elt.getStringAttribute("id");
          if(identifier.equals(prefName)) {
            return elt.getContent();
          }
        }
      }
      return null;
    } catch (IOException e) {
      return null;    
    }   
  }

  
  /**
   * Returns true if the named user preference is set to true.
   *
   * @param prefName the preference name
   * @return true if the preference is set to true.
   */
  public static boolean isUserPreference(String prefName) {
    String value = getUserPreference(prefName);
    return (value != null) && Boolean.valueOf(value).booleanValue();
  }


  /**
   * Sets a boolean user preference.
   *
   * @param prefName the preference name
   * @param enabled the boolean preference value.
   */
  public static void setUserPreference(String prefName, boolean enabled) {
    setUserPreference(prefName, Boolean.toString(enabled));
  }


  /**
   * Sets a user preference. This convenience method catches any
   * IOException during preference saving (the preference will only
   * last for the duration of the session).  If you don't like this
   * behaviour, use updateWithChild or savePreferenceToFile directly.
   *
   * @param prefName the preference name
   * @param value the preference value.
   */
  public static void setUserPreference(String prefName, String value) {
    try {
      savePreferenceToFile(prefName, value);
    } catch (IOException ioe) {
      ioe.printStackTrace(); // Preference will only last for this session
    }
  }


  /**
   * This method updates the irate.xml file, given an actual XMLElement.  If
   * the element already exists, it will be replaced.
   * @param child
   * @throws IOException
   */
  public static void updateWithChild(XMLElement child) throws IOException {
    
    XMLElement docElt = new XMLElement(new Hashtable(), false, false);
    boolean foundFlag = false;
    
    if(config.exists()) {
      docElt = getConfigFileAsXML();
      Enumeration e = docElt.enumerateChildren();
      while(e.hasMoreElements()) {
        XMLElement elt = (XMLElement)e.nextElement();
          if (elt.getName().equals(child.getName())) {
            String identifier = elt.getStringAttribute("id");
            if(identifier.equals(child.getAttribute("id"))) {
              docElt.removeChild(elt);
              docElt.addChild(child);
              foundFlag = true;
            }
          }
       }
     }
     
     if(!config.exists() || foundFlag == false) {
       if(!config.exists()) {
        docElt.setName("irate");
       }
       docElt.addChild(child);
     }
     writeConfigToFile(docElt);
  }

  
  /**
   * Add a preference to the irate.xml file.  
   * @param prefName The id of the preference
   * @param prefValue The value of the preference
   * @throws IOException
   */
  public static void savePreferenceToFile(String prefName, String prefValue) throws IOException {
    XMLElement pref = new XMLElement(new Hashtable(), false, false);
    pref.setName("preference");
    pref.setAttribute("id", prefName);
    pref.setContent(prefValue);
    updateWithChild(pref);
  }

  
  /**
   * Generate the irate.xml file based upon an an XMLElement representation
   * of the element.
   * @param document
   * @throws IOException
   */
  private static void writeConfigToFile(XMLElement document) throws IOException {
    
    FileWriter fw = new FileWriter(temp);
    try {
      
      fw.write("<?xml version=\"1.0\"?>\n");
      fw.write(document.toString());
      fw.write("\n");
      fw.close();
      fw = null;

      // If we wrote the file successfully, then rename the temporary
      // file to the real name of the configuration file.  This makes
      // the writing of the new file effectively atomic.
      if (!temp.renameTo(config)) {
        config.delete();
      if (!temp.renameTo(config))
        throw new IOException("Failed to rename "+temp+" to "+config);
      }
    }finally {
      if (fw != null)
        fw.close();
      }
  }
  
  /** 
   * Reads the current irate.xml file into an XMLElement and returns the element.
   * @return An XMLElement containing the contents of irate.xml
   * @throws IOException
   */
  private static XMLElement getConfigFileAsXML() throws IOException {
    
    XMLElement docElt = new XMLElement(new Hashtable(), false, false);
      
    FileInputStream inputStream = new FileInputStream(config);
    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
    docElt.parseFromReader(inputStreamReader);
    inputStreamReader.close();
    inputStream.close();
    
    return docElt;
    
  }

}
