package irate.client;

import java.io.*;

public class Speech {

  private String synthPath = "/usr/bin/festival";
  private boolean supported;
  private Process synthProcess;
  private InputStream is;
  private OutputStream os;
  private boolean first = true;
  
  public Speech() {
    supported = new File(synthPath).exists();
  }

  public boolean isSupported() {
    return supported;
  }

  public void say(String text) throws IOException {
    if (!supported)
      return;

    if (first) {
      festival("(set! after_synth_hooks (list (lambda (utt) (utt.wave.rescale utt 8.0))))");
      festival("(Parameter.set 'Duration_Stretch 1.5)");
      first = false;
    }
    festival("(SayText \"" + text.replace('"', ' ') + "\")");
  }
  
  private char getch() throws IOException {
    int ch = is.read();
    if (ch < 0)
      throw new IOException("Unexpected end of file");
    System.out.print((char) ch);
    return (char) ch;
  }

  private void festival(String s) throws IOException {
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
    
    System.out.println(s);
    os.write((s + "\n").getBytes());
    os.flush();

    while (true)
      if (getch() == '>')
        if (getch() == ' ')
          break;
   
    System.out.println();
  }
}
