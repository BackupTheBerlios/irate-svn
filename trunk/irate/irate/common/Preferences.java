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

  public Preferences() {
    home = new File(System.getProperties().getProperty("user.home"));
    config = new File(home, "/irate/irate.xml");
    temp = new File(home, "/irate/irate.xml~");
  }
      
  /** 
   * This method accesses the irate.xml configuration file and returns a 
   * string representation of the user's preferred download directory.  That is,
   * the directory where trackdatabase.xml is stored.
   * @return the directory location of where the trackdatabase.xml file is stored
   */
  public static String getUserDownloadDirectoryPreference() {
   
    try {    
      
      XMLElement docElt = getConfigFileAsXML();
      
      Enumeration enum = docElt.enumerateChildren();
      while(enum.hasMoreElements()) {
        XMLElement elt = (XMLElement)enum.nextElement();
        if (elt.getName().equals("preference")) {
          String identifier = elt.getStringAttribute("id");
          if(identifier.equals("downloadDir")) {
            return elt.getContent();
          }
        }
      }
      return null;
    } catch (IOException e) {
      return null;    
    }
  }
  
  public static String getUserPreference(String prefName) 
  {
    try {    
     XMLElement docElt = getConfigFileAsXML();
  
     Enumeration enum = docElt.enumerateChildren();
     while(enum.hasMoreElements()) {
       XMLElement elt = (XMLElement)enum.nextElement();
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
    
      Enumeration enum = docElt.enumerateChildren();
      
      while(enum.hasMoreElements()) {
        XMLElement elt = (XMLElement)enum.nextElement();
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
    
    boolean foundFlag = false;

    XMLElement docElt = new XMLElement(new Hashtable(), false, false);
    
    if(config.exists()) {
      
     
     docElt = getConfigFileAsXML();
     
     
     Enumeration enum = docElt.enumerateChildren();
      while(enum.hasMoreElements()) {
        XMLElement elt = (XMLElement)enum.nextElement();
        if (elt.getName().equals("preference")) {
          String identifier = elt.getStringAttribute("id");
          if(identifier.equals(prefName)) {
            elt.setContent(prefValue);
            foundFlag = true;
          }
        }
      }
    }
    
    if(!config.exists() || foundFlag == false) {
      if(!config.exists()) {
        docElt.setName("irate");
      }
      XMLElement newPref = new XMLElement(new Hashtable(), false, false);
      newPref.setName("preference");
      newPref.setAttribute("id", prefName);
      newPref.setContent(prefValue);
      docElt.addChild(newPref);
    }
    
    writeConfigToFile(docElt);
    
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
