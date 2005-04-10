package irate.common;

import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;

public class LicensingScheme implements Comparable {

  private URL url;
  private String name;
  private String icon;
  private String fullText;
  
  private Hashtable imageTable;
	
  // Build a Licensing Scheme with an URL, name and icon filename.
  public LicensingScheme(URL url, String name, String fullText, String icon) {
    initializeLicensingScheme(url, name, fullText, icon);
  }

  // Build a Licensing Scheme given the copyright Data for a 
  // track.
  public LicensingScheme(String copyrightData) {
    buildImageTable();
    if(copyrightData == null || copyrightData.equals("")) { //$NON-NLS-1$
      initializeLicensingScheme(null, "",
Resources.getString("LicensingScheme.No_Copyright_Information_Available"), "");
    }
    else {	
      String urlFound = findURLInText(copyrightData);
      String nameFound = null;
			
      if(urlFound != null && !urlFound.equals("")) { //$NON-NLS-1$
        nameFound = findNameInURL(urlFound);	
        try {
          if (nameFound != null)
            initializeLicensingScheme(new URL(urlFound), nameFound, copyrightData, (String)imageTable.get(nameFound));
        }
        catch(Exception e) {
          e.printStackTrace();	
        }
      }
      else {
        initializeLicensingScheme(null, "", copyrightData, ""); //$NON-NLS-1$ //$NON-NLS-2$
      }
    }
  }
	
  private void buildImageTable() {
    imageTable = new Hashtable();
    imageTable.put("creativecommons","creativecommons.gif"); //$NON-NLS-1$ //$NON-NLS-2$
    imageTable.put("foobar", "foobar.gif"); //$NON-NLS-1$ //$NON-NLS-2$
  }
		
  private String findNameInURL(String urlFound) {
  	
    // Search for any name that we have in the hashtable.
    String currentKey = null;
    Enumeration allKeys = imageTable.keys();
  	 	
    while (allKeys.hasMoreElements()) {
      currentKey = (String)allKeys.nextElement();
      if(urlFound.indexOf(currentKey) != -1) 
      {
        return currentKey;
      }	
    }  	 	
    return null;
  }

  private String findURLInText(String copyrightData) {
    int urlStart = copyrightData.indexOf("http://"); //$NON-NLS-1$
    if(urlStart != -1) { //$NON-NLS-1$
      int urlEnd = copyrightData.indexOf(" ", urlStart); //$NON-NLS-1$
		  if(urlEnd == -1)
		    urlEnd = copyrightData.length();
      return copyrightData.substring(urlStart, urlEnd);
    }
    return null;
  }

  public void initializeLicensingScheme(URL url, String name, String fullText, String icon) {
    this.url = url;
    this.name = name;
    this.fullText = fullText;
    this.icon = icon;
  }

  public URL getURL() { return url; }
  public String getName() { return name; }
  public String getIcon() { return icon; }

  public String getFullText() {
    return fullText;
  }
  
  public int compareTo(Object o) {
    if (!(o instanceof LicensingScheme))
      return -1;
    LicensingScheme license = (LicensingScheme) o;
    return getName().compareTo(license.getName());
  }
}
