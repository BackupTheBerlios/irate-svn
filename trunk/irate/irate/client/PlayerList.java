// Copyright 2003 Anthony Jones

import java.util.Vector;

public class PlayerList {

  private Player[] players;
  
  public PlayerList() {
    Vector players = new Vector();
    try {
      players.add(Class.forName("JavaLayerPlayer").newInstance());
    } 
    catch (ClassNotFoundException e) {
    }
    
    try {
      players.add(new ExternalPlayer("mpg123", new String[] { "/usr/bin/mpg123", "/usr/local/bin/mpg123" }));
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    
    try {
      players.add(new ExternalPlayer("madplay", new String[] { "madplay", "/usr/bin/madplay", "/usr/local/bin/madplay", "madplay.exe" });
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  public Player[] getPlayers() {
    return players;
  }
}
