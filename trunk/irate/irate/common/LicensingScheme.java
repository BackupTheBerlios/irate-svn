package irate.common;

import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LicensingScheme {

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
		if(copyrightData == null || copyrightData.equals("")) {
			initializeLicensingScheme(null, "", "No Copyright Information Available", "");
		}
		else {
			
			String urlFound = findURLInText(copyrightData);
			String nameFound = null;
			
			if(urlFound != null && !urlFound.equals("")) {
				nameFound = findNameInURL(urlFound);	
				try {
					initializeLicensingScheme(new URL(urlFound), nameFound, copyrightData, (String)imageTable.get(nameFound));
				}
				catch(Exception e) {
					e.printStackTrace();	
				}
			}
			else {
				initializeLicensingScheme(null, "", copyrightData, "");
			}
			}
		}
	
	private void buildImageTable() {
			imageTable = new Hashtable();
			imageTable.put("creativecommons","images/license/creativecommons.gif");
			imageTable.put("foobar", "images/license/foobar.gif");
	}
		
  private String findNameInURL(String urlFound) {
  	
  	// Search for any name that we have in the hashtable.
  	StringBuffer buffer = new StringBuffer();
  	Enumeration allKeys = imageTable.keys();
  	 	
		while (allKeys.hasMoreElements()) {
  	 		buffer.append((String)allKeys.nextElement());
				if(allKeys.hasMoreElements()) {
					buffer.append("|");
				}
		}  	 		

 		String regex = "(" + buffer.toString() + ")";
		Pattern pattern = Pattern.compile(regex);
		Matcher match = pattern.matcher(urlFound);
			
			// If we find one, this is the name
			if(match.find()) {
				return match.group();	
			}
			return null;
  }

  private String findURLInText(String copyrightData) {
			
		// Find the first web address in the copyright Data. 
		String regex = "(http://[\\w[.][/]]*)";
		Pattern pattern = Pattern.compile(regex);
		Matcher match = pattern.matcher(copyrightData);
		
		// If we find one, this is the URL.
		if(match.find()) {
			return match.group();	
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

}
