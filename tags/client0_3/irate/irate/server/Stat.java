package irate.server;

import java.io.IOException;

import irate.common.*;

public class Stat {

  public static void main(String args[]) {
    Stat stat = new Stat();
    stat.process();
  }
 
  private UserList userList;
  private DatabaseReference[] users;
  
  public Stat() {
    userList = new UserList();
    users = userList.getUsers();
  }

  public void process() {
    for (int i = 0; i < users.length; i++) {
      try {
        ServerDatabase user = users[i].getServerDatabase();
        Track[] tracks = user.getTracks();
        float total = 0;
        int count = 0;
        for (int j = 0; j < tracks.length; j++) {
          Track track = tracks[j];
          if (track.isRated()) {
            total += track.getRating();
            count++;
          }
        }
        if (count != 0)
          System.out.println(count + " " + total / count);      
        users[i].discard();
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
