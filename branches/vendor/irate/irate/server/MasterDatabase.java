package irate.server;

import irate.common.*;

import java.io.*;
import java.util.*;

public class MasterDatabase extends ServerDatabase {

  private UserList userList;
  private Random random = new Random();
  private ServerDatabase orphans;

    // The user rate is the number of songs the database must have for each
    // track the user is allowed.
  private int userRate = 3;
  
  public MasterDatabase(File file, UserList userList) throws IOException {
    super(file);
    this.userList = userList;
  }
  
  public ServerDatabase processRequest(ServerDatabase request) {
    ServerDatabase reply = new ServerDatabase();

    System.out.println("User " + request.getUserName() + " Password " + request.getPassword());
    ServerDatabase user = userList.getUser(request.getUserName());

      // If the user doesn't exist or the password is incorrect the return a 
      // blank response.
    if (user == null) {
      reply.setError("user", "file:help/user.html");
      return reply;
    }

    if (!user.getPassword().equals(request.getPassword())) {
      reply.setError("password", "file:help/password.html");
      return reply;
    }

      // Update the users ratings. 
    user.update(request);

    if (user.getNoOfTracks() * userRate >= getNoOfTracks()) {
      reply.setError("gotnone", "file:help/getstuffed.html");
      return reply;
    }
        // If there are any orphans then we add one here
    if (orphans == null)
      orphans = findOrphans();
    Track track = orphans.randomTrack(random);
    if (track != null) {
      System.out.print("Orphan: " + track.toString());
  
      user.add(track);
      reply.add(track);
      orphans.remove(track);
    }
    else { // No orphans so we need to get any track which the user doesn't already have.
      ServerDatabase spares = getSpares(user);
      track = spares.randomTrack(random);
      if (track != null) {
        System.out.println("Spare: " + track.toString());
        user.add(track);
        reply.add(track);
      }
      else {
        reply.setError("empty", "file:help/empty.html");
        return reply;
      }
    }

      // Save the user database
    try {
      user.save();
    }
    catch (IOException e) {
      e.printStackTrace();
    }

    return reply;
  } 
  
  public ServerDatabase findOrphans() {
    ServerDatabase orphans = new ServerDatabase();
    Track[] tracks = getTracks();
    ServerDatabase[] users = userList.getUsers();
    for (int i = 0; i < tracks.length; i++) {
      add: {
        for (int j = 0; j < users.length; j++) 
          if (users[j].getTrack(tracks[i]) != null)
            break add;
        orphans.add(tracks[i]);
      }
    }
    return orphans;
  }

  public ServerDatabase getSpares(ServerDatabase user) {
    ServerDatabase spares = new ServerDatabase();
    Track[] tracks = getTracks();
    for (int i = 0; i < tracks.length; i++) 
      if (user.getTrack(tracks[i]) == null)
        spares.add(tracks[i]);
    return spares;
  }
}
