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

  public void say(String text) throws IOException {
    if (!supported)
      return;

    festival("(SayText \"" + text.replace('"', ' ') + "\")");
  }
  
  private char getch() throws IOException {
    int ch = is.read();
    if (ch < 0)
      throw new IOException("Unexpected end of file");
//    System.out.print((char) ch);
    return (char) ch;
  }

  private void startSynthProcess() throws IOException {
    if (!supported)
      return;

    if (synthProcess == null) {
      synchronized (this) {
	synthProcess = Runtime.getRuntime().exec(new String[] { synthPath, "--interactive" });
      }
      os = synthProcess.getOutputStream();
      is = synthProcess.getInputStream();
      while (true)
        if (getch() == '>')
          if (getch() == ' ')
            break;

      festival("(set! after_synth_hooks (list (lambda (utt) (utt.wave.rescale utt 4.0))))");
      festival("(Parameter.set 'Duration_Stretch 1.5)");
    }
  }

  private void festival(String s) throws IOException {
      // Do nothing if there's no speech executable.
    if (!supported)
      return;
    
    startSynthProcess();

    while (is.available() > 0)
      getch();
    
//    System.out.println(s);
    os.write((s + "\n").getBytes());
    os.flush();

    while (true)
      if (getch() == '>')
        if (getch() == ' ')
          break;
   
//    System.out.println();
  }

  public synchronized void abort() {
    if (synthProcess != null) {
      synthProcess.destroy();
      try {
	synthProcess.waitFor();
      }
      catch (InterruptedException e) {
      }
      finally {
	synthProcess = null;
      }
    }
  }
}
