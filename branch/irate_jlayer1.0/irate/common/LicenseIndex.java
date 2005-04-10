/*
 * Created on Apr 22, 2004
 */
package irate.common;

import java.util.Hashtable;


/**
 * Hash table to reduce the number of instances of LicensingScheme required.
 * This class means there only needs to be one instance for each unique license
 * URL.
 *   
 * @author Anthony Jones
 */
public class LicenseIndex {
  
   private Hashtable hashtable = new Hashtable();
   private LicensingScheme nullLicense = new LicensingScheme(null);
   
   public LicenseIndex() {
   }
   
   public LicensingScheme get(Track track) {
     String copyrightInfo = track.getCopyrightInfo();
     if (copyrightInfo == null)
       return nullLicense;
     
     LicensingScheme license = (LicensingScheme) hashtable.get(copyrightInfo);
     if (license == null)
       hashtable.put(copyrightInfo, license = new LicensingScheme(copyrightInfo));
     
     return license;
   }
}
