// Copyright 2003 Anthony Jones

package irate.client;

import java.io.*;
import java.text.DecimalFormat;

public class Speech {

  private String synthPath = "/usr/bin/festival";
  private boolean supported;
  private Process synthProcess;
  private InputStream is;
  private OutputStream os;
  private int volumeOffset;
  private boolean toSetVolume;
  
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

      toSetVolume = true;
    }
  }

  private static DecimalFormat df = new DecimalFormat("0.0");

  private void festival(String s) throws IOException {
      // Do nothing if there's no speech executable.
    if (!supported)
      return;
    
    try {
      startSynthProcess();

      if (toSetVolume) {
          // Convert the volume offset in decibels into an amplitude.
        double amplitude = 4.0 * Math.pow(10.0, ((double) volumeOffset / 20.0));
        String amplitudeStr = df.format(amplitude);
        // System.out.println("vo="+volumeOffset+" - set speech amplitude to "+amplitudeStr);
        doFestival("(set! after_synth_hooks (list (lambda (utt) (utt.wave.rescale utt "+amplitudeStr+"))))");
        doFestival("(Parameter.set 'Duration_Stretch 1.5)");
        toSetVolume = false;
      }
      doFestival(s);
    }
    catch (IOException e) {
      abort();
      throw e;
    }
  }

  private void doFestival(String s) throws IOException {
    while (is.available() > 0)
      getch();
    
    os.write((s + "\n").getBytes());
    os.flush();

    while (true)
      if (getch() == '>')
        if (getch() == ' ')
          break;
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

  public void setVolume(int volume)
  {
      Speech.volumeOffset = volume;
      toSetVolume = true;
  }
}
