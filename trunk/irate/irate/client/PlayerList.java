// Copyright 2003 Anthony Jones
package irate.client;

import java.util.Vector;
import java.io.FileNotFoundException;

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
      players.add(new MadplayPlayer());
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    try {
      players.add(new ExternalPlayer("mpg123", new String[] { "mpg123", "/usr/bin/mpg123", "/usr/local/bin/mpg123" }));
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    
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
