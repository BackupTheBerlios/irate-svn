package irate.client;

import java.io.*;

public class Speech {

  private String synthPath = "/usr/bin/festival";
  private boolean supported;
  private Process synthProcess;
  private InputStream is;
  private OutputStream os;
  
  public Speech() {
    supported = new File(synthPath).exists();
  }

  public boolean isSupported() {
    return supported;
  }

  private char getch() throws IOException {
    int ch = is.read();
    if (ch < 0)
      throw new IOException("Unexpected end of file");
    System.out.print((char) ch);
    return (char) ch;
  }
  
  public void say(String text) throws Exception {
      // Do nothing if there's no speech executable.
    if (!supported)
      return;
    
    if (synthProcess == null) {
      synthProcess = Runtime.getRuntime().exec(new String[] { synthPath, "--interactive" });
      os = synthProcess.getOutputStream();
      is = synthProcess.getInputStream();
      while (true)
        if (getch() == '>')
          if (getch() == ' ')
            break;
    }
    while (is.available() > 0)
      getch();
    
    String s = "(SayText \"" + text.replace('"', ' ') + "\")\n";
    System.out.println(s);
    os.write(s.getBytes());
    os.flush();

    while (true)
      if (getch() == '>')
        if (getch() == ' ')
          break;
   
    System.out.println();
  }
}
