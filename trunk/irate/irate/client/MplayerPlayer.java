// Copyright 2003 Anthony Jones

package irate.client;

import java.io.FileNotFoundException;
import java.io.File;

public class MplayerPlayer extends ExternalPlayer {

  private int trackLength = 0;

  public MplayerPlayer()
      throws FileNotFoundException
  {
    super(new String[] { "mplayer" });
  }
  
  public String[] formatResumeArgument() {
    return new String[] { "-ss", formatTime(getPlayTime()) };
  }
  
  public String[] formatVolumeArgument() {
    int volume = getVolume();
    // Set the volume in the acceptable range for mplayer
    if (volume < -200) volume = -200;
    if (volume > 60) volume = 60;
    return new String[] { "-af", "volume=" + Integer.toString(volume) };
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
    super.play(file);
  }
  
   /**
    * Analyses the player's output to obtain the current position in track. Sends
    * notify to listeners when done.
    */
  public boolean processPlayerTrackOutput(String line) {
    if (!line.startsWith("A:"))
      return false;

    String[] splits;
    String data;
    int seconds, minutes, hours, time;
    
    try {
      data = line.substring(2).trim();
      splits = split(data, ' '); // isolate position indicator      
      splits = split(splits[0], ':'); // split in hours, minutes & seconds

      time = 0;
      for (int i = 0; i < splits.length; i++) {
        if (splits[i] != null) {
          time *= 60;
          time += (int) Float.parseFloat(splits[i]);
        }
      } 
      
      notifyPosition(trackLength - time, trackLength);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return true;  
  }
     
  public String getName() {
    return "mplayer";
  }

}
