// Copyright 2003 Anthony Jones

package irate.client;

import java.io.FileNotFoundException;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.*;

public class MadplayPlayer extends ExternalPlayer {

  private boolean acquireNewTrackLength = true;
  private int trackLength = 0;

  public MadplayPlayer()
      throws FileNotFoundException
  {
    super(new String[] { "madplay", "madplay.exe" });
  }

  
  private String format00(int number)
  {
    if (number < 10)
      return "0"+Integer.toString(number);
    else
      return Integer.toString(number);
  }

  public String[] formatResumeArgument()
  {
    long playTime = getPlayTime();
    int seconds = (int) ((playTime / 1000L) % 60L);
    int minutes = (int) ((playTime / 60000L) % 60L);
    int hours = (int) (playTime / 3600000L);
    return new String[] {
      "--start="+Integer.toString(hours)+":"+format00(minutes)+":"+format00(seconds)
    };
  }
  
  public String[] formatVolumeArgument()
  {
    int volume = getVolume();
      // Don't allow the volume to go outside the range accepted by madplay, or it
      // won't play it.
    if (volume < -175) volume = -175;
    if (volume > 18) volume = 18;
    return new String[] { "--amplify", Integer.toString(volume) };
  }
  
  public String[] formatOtherArguments() 
  {
    // We make the player output the remaining time in order to obtain the lenght of the track at the beginning
    return new String[] { "-v", "--display-time=remaining" };
  }
  
  /*
   * New file is being played -- reset track lenght
   */
  public void play(File file) throws PlayerException {
    acquireNewTrackLength = true;
    super.play(file);
  }
  
  /*
   * Hacked route() routine used to grab player output from stdout
   */
   public void route(final InputStream is, final OutputStream os, String title) {
    new Thread(title) {
      public void run() {
        String currentLine;
        
        try {
          BufferedReader in = new BufferedReader(new InputStreamReader(is));
          BufferedWriter out = new BufferedWriter(new OutputStreamWriter(os));
          
          while ((currentLine=in.readLine()) != null) {
            if (currentLine.charAt(0) != '-') {
              out.write(currentLine + '\n');
              out.flush();
            }
            else {
              processPlayerTrackOutput(currentLine);
              out.flush();
            }
          } 
          in.close();
          out.close();
        } 
        catch (IOException ioe) {
          //ioe.printStackTrace();
        }
      }
    }.start();
  }
   
   /**
    * Analyses the player's output to obtain the current position in track. Sends
    * notify to listeners when done.
    */
  public void processPlayerTrackOutput(String line) {
    String[] splits;
    String data;
    int seconds, minutes, hours, time;
    
    try {
      data = line.substring(1); // remove first char (negative sign)
      //splits = data.split(" ");
      //splits = splits[0].split(":");
      splits = split(data, ' '); // isolate position indicator
      splits = split(splits[0], ':'); // split in hours, minutes & seconds

      hours = Integer.valueOf(splits[0]).intValue();
      minutes = Integer.valueOf(splits[1]).intValue();
      seconds = Integer.valueOf(splits[2]).intValue();
      
      time = hours * 3600 + minutes * 60 + seconds;
      
      if (acquireNewTrackLength) {
        // if a new track started, use the first event we get from the player to calculate
        // the lenght of the track
        trackLength = time;
        acquireNewTrackLength = false;
      }
      
      notifyPosition(trackLength - time, trackLength);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Splits a string in multiple substrings. Implemented here instead of using String.split() in order
   * to remain compatible with JDK 1.3. Maximum of 4 string divisions.
   */
  private String[] split(String data, char divisor) {
    String[] temp = new String[4];
    int currentSubString = 0;
    int lastpos = 0;

    while (firstIndexOf(data, divisor, lastpos) != -1 && currentSubString != temp.length - 1) {
      temp[currentSubString] = data.substring(lastpos, firstIndexOf(data, divisor, lastpos));
      lastpos = firstIndexOf(data, divisor, lastpos) + 1;
      currentSubString++;
    }
    
    temp[currentSubString] = data.substring(lastpos);

    return temp;
  }
  
  private int firstIndexOf(String data, char c, int pos) {
    for (int i = pos; i < data.length(); i++)
      if (data.charAt(i) == c)
        return i;
    
    return -1;
  }
   
  public String getName() {
    return "madplay";
  }

}
