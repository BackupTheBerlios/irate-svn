// Copyright 2003 Anthony Jones

package irate.client;

import java.io.FileNotFoundException;
import java.io.File;

public class MadplayPlayer extends ExternalPlayer {

  private boolean acquireNewTrackLength = true;
  private int trackLength = 0;

  public MadplayPlayer()
      throws FileNotFoundException
  {
    super(new String[] { "madplay", "madplay.exe" });
  }
  
  public String[] formatResumeArgument() {
    return new String[] { "--start="+formatTime(getPlayTime()) };
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
  
   /**
    * Analyses the player's output to obtain the current position in track. Sends
    * notify to listeners when done.
    */
  public boolean processPlayerTrackOutput(String line) {
    if (!line.startsWith("-"))
      return false;
    
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
    return true;
  }
     
  public String getName() {
    return "madplay";
  }

}
