package irate.version;

import java.io.*;
import java.net.URL;

/** Common application version for all iRATE applications.
    The version information is read from the resource files
    irate/version/version.txt and irate/version/timestamp.txt.
    timestamp.txt is created by the build system.
    version.txt is edited by the maintainer and should contain
    a one-line version string only for releases, otherwise be empty.
*/
public class Version {
  
  /** Returns a version string.
      No assumptions about the format should be made.
      Returns an empty string if no information is available.
  */
  public static String getVersionString() {
    String version = parseResourceFile("irate/version/version.txt");
    if (version.equals("")) {
      String timeStamp = parseResourceFile("irate/version/timestamp.txt");
      if (! timeStamp.equals(""))
        version = "[Build " + timeStamp + "]";
    }
    return version;
  }
  
  private static String parseResourceFile(String filename) {
    URL resource = ClassLoader.getSystemClassLoader().getResource(filename);
    if (resource == null)
      return "";
    try {
      InputStream inputStream = resource.openStream();
      Reader reader = new InputStreamReader(inputStream);
      BufferedReader bufferedReader = new BufferedReader(reader);
      String line = bufferedReader.readLine();
      if (line == null)
        return "";
      bufferedReader.close();
      return line.trim();
    }
    catch (java.io.IOException e) {
      return "";
    }
  }
}

// Local Variables:
// c-file-style:"gnu"
// c-basic-offset:2
// indent-tabs-mode:nil
// tab-width:4
// End:
