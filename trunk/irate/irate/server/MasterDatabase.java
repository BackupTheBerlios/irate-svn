package irate.server;

import irate.common.*;

import java.io.*;
import java.util.*;

public class MasterDatabase extends ServerDatabase {

    // Add an orphan track one time in n.
  private final int orphanChance = 10;

    // Add a random track one time in n.
  private final int randomChance = 25;
  
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
      reply.setError("user", "user.html");
      return reply;
    }

    if (!user.getPassword().equals(request.getPassword())) {
      reply.setError("password", "password.html");
      return reply;
    }

      // Update the users ratings. 
    user.update(request);

    if (user.getNoOfTracks() * userRate >= getNoOfTracks()) {
      reply.setError("gotnone", "getstuffed.html");
      return reply;
    }

    if ((random.nextInt() % orphanChance) == 0) {
        // If there are any orphans then we add one here
      if (orphans == null)
        orphans = findOrphans();
      Track track = orphans.randomTrack(random);
      if (track != null) {
        System.out.println("Orphan: " + track.getName());
    
        user.add(track);
        reply.add(track);
        orphans.remove(track);
      }
    }

      // See if we can correlate a track
    ServerDatabase corel = getBest(user);
    Track track = corel.randomTrack(random);
    if (track != null) {
      System.out.println("Correlation: " + track.getName() + " " + track.getRating());
      user.add(track);
      reply.add(track);
    }

      // Do this randomly or if we couldn't correlate
    if (track == null || (random.nextInt() % randomChance) == 0) {
        // Just pick any random track that we don't already have
      ServerDatabase spares = getSpares(user);
      track = spares.chooseTrack(random);
      if (track != null) {
        System.out.println("Random: " + track.getName());
        user.add(track);
        reply.add(track);
      }
    }

      // If we couldn't find any tracks to add then show a dialog.
    if (reply.getNoOfTracks() == 0)
      reply.setError("empty", "empty.html");
    
      // Save the user database
    try {
      user.save();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    
    purge(reply);
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

  public ServerDatabase getBest(ServerDatabase user) {
    ServerDatabase best = new ServerDatabase();
    ServerDatabase[] users = userList.getUsers();
    TrackAverageRating tar = new TrackAverageRating();
    for (int i = 0; i < users.length; i++) {
      if (user != users[i]) {
        DatabaseCorrelator dc = new DatabaseCorrelator(user, users[i]);
        dc.process();
        if (dc.getCorrelation() > 0) {
          System.out.println("Friend: " + users[i].getUserName() + " " + dc.getCorrelation());
          tar.add(dc.getSpares());
        }
      }
    }
    return tar.getAverages();
  }
  
  public void purge(ServerDatabase db) {
    Track[] tracks = db.getTracks();
    for (int i = 0; i < tracks.length; i++) 
      tracks[i].setUnrated();
  }
}
