// Copyright 2003 Anthony Jones

package irate.client;

import java.io.FileNotFoundException;

public class MadplayPlayer extends ExternalPlayer {

  public MadplayPlayer()
    throws FileNotFoundException
  {
    super("madplay", new String[] { "madplay", "/usr/bin/madplay", "/usr/local/bin/madplay", "madplay.exe" });
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
    return new String[] { "--amplify", Integer.toString(getVolume()) };
  }
}
