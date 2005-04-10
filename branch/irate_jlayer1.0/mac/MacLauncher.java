package mac;

// Quick ugly mac launcher; unzips the app bundle and executes it.
// Java Web Start doesn't currently work directly with SWT apps on Mac OS X as
// SWT needs a tweaked java launcher.
//
// (c) 2004 Brion Vibber, GPL blah blah

import java.io.*;
import java.util.zip.*;

public class MacLauncher {
  public static void main (String args[]) {
    MacLauncher launcher = new MacLauncher();
  }
  
  public MacLauncher() {
    String destdir = "/tmp/irate." + System.getProperties().getProperty("user.name") + "/";
    System.out.println("Hello! Going to put irate in " + destdir);
    try {
      new File(destdir).mkdir();
    } catch (Exception e) {
      //
    }
    if (unzipApp(getClass().getResourceAsStream("irate-client-macosx.zip"), destdir)) {
      System.out.println("Fixing exec bit...");
      fixExecuteBit(destdir + "iRATE.app/Contents/MacOS/java_swt");
      
      System.out.println("Executing...");
      runApp(destdir + "iRATE.app");
    }
  }
  
  private boolean unzipApp(InputStream zipstream, String destdir) {
    try {
      System.out.println("Trying to extract zip");
      ZipInputStream zip = new ZipInputStream(zipstream);
      ZipEntry each;
      while ((each = zip.getNextEntry()) != null) {
        String fname = each.getName();
        if (fname.startsWith("iRATE.app")) {
          if (fname.endsWith("/")) {
            extractDirectory(zip, fname, destdir);
          } else {
            extractFile(zip, fname, destdir);
          }
        } else {
          System.out.println("ignoring: " + fname);
        }
      }
      zip.close();
      return true;
    } catch(Exception e) {
      e.printStackTrace();
      return false;
    }
  }
  
  private void extractFile(ZipInputStream zip, String fname, String destdir)
    throws IOException, ZipException {
    System.out.println("Extracting: " + fname);
    FileOutputStream out = new FileOutputStream(destdir + fname);
    final int bufsize=4096;
    byte buf[] = new byte[bufsize];
    int bytesRead;
    while ((bytesRead = zip.read(buf, 0, bufsize)) != -1) {
      out.write(buf, 0, bytesRead);
    }
    out.close();
  }
  
  private void extractDirectory(ZipInputStream zip, String fname, String destdir)
    throws IOException {
    System.out.println("Directory: " + fname);
    new File(destdir + fname).mkdir();
  }
  
  private static void fixExecuteBit(String fname) {
    try {
      Runtime.getRuntime().exec("/bin/chmod a+x " + fname );
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  private static void runApp(String fname) {
    try {
      Runtime.getRuntime().exec("/usr/bin/open " + fname );
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
}
