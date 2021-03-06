// Copyright 2003 Anthony Jones
package irate.client;

import java.util.Vector;

public class PlayerList {

  private Player[] players;
  
  public PlayerList() {
    Vector players = new Vector();
    
    try {
    	//seems like reflection of classes with unknown members is nicely broken in gcj
    	if(System.getProperty("java.vm.name").indexOf("gcj") == -1)
      		players.add(Class.forName("irate.client.JavaLayerPlayer").newInstance());
    } 
    catch (Exception e) {
    	e.printStackTrace();
    }
    
    try {
      // Don't try this on Windows -- it will start
      // "mplayer.exe" which is not what we want at all.
      if (!System.getProperty("os.name").toLowerCase().startsWith("win"))
        players.add(Class.forName("irate.client.MplayerPlayer").newInstance());
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    try {
      players.add(Class.forName("irate.client.MadplayPlayer").newInstance());
      //players.add(new MadplayPlayer());
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    
// I've removed this because it doesn't really work as well as it should do.
//    try {
//      players.add(Class.forName("irate.client.StreamPlayer").newInstance());
//    }
//    catch (Exception e) {
//      e.printStackTrace();
//    }

    this.players = (Player[]) players.toArray(new Player[players.size()]);
  }

  public Player[] getPlayers() {
    return players;
  }

  public Player getPlayer(String name) {
    if (players.length == 0)
      return null;
    for (int i = 0; i < players.length; i++) {
      if (name.equals(players[i].getName()))
        return players[i];
    }
    return players[0];
  }
}
