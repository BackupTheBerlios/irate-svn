// Copyright 2003 Anthony Jones

package irate.server;

import irate.common.*;

import java.io.*;
import java.util.*;

public class MasterDatabase extends ServerDatabase {

    /** Add an orphan track one time in n. */
  private final int orphanChance = 10;

    /** Add a random track one time in n. */
  private final int randomChance = 20;

    /** The number of peer tracks to start with. */
  private final int initialPeerTracks = 7;

    /** The number of peer tracks to download before switching to random. */
  private final int peerThreshhold = 14;

    /** The number of times it will pick a user in order to get a random track. */
  private final int peerRetries = 10;
  
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

    System.out.println("User " + request.getUserName());
//    System.out.println("User " + request.getUserName() + " Password " + request.getPassword());
    ServerDatabase user = userList.getUser(request.getUserName());

      // If the user doesn't exist or the password is incorrect the return a 
      // blank response.
    if (user == null) {
        // If the user already has tracks
      if (request.getNoOfTracks() != 0 || request.getUserName().length() == 0 || request.getPassword().length() == 0) {
        reply.setError("user", "user.html");
        return reply;
      }
      user = userList.createUser(request.getUserName(), request.getPassword());
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
    
        reply.add(track);
        orphans.remove(track);
      }
    }

      // See if we can correlate a track
    if (user.getNoOfTracks() >= initialPeerTracks) {
      ServerDatabase corel = getBest(user);
      Track track = corel.chooseTrack(random);
      if (track != null) {
        System.out.println("Correlation: " + track.getName() + " " + track.getRating());
        reply.add(track);
      }
    }

      // Correlate tracks for new users
    if (reply.getNoOfTracks() == 0 && user.getNoOfTracks() < peerThreshhold) {
      for (int i = 0; i < peerRetries; i++) {
        ServerDatabase peer = userList.randomUser(random, user);
        if (peer != null) {
          System.out.println("Peer: " + peer.getUserName());
          Track track = peer.chooseTrack(random);
          if (track != null) {
            System.out.println("Peer track: " + track.getName() + " " + track.getRating());
            reply.add(track);
            break;
          }
        }
      }
    }
        
      // Do this randomly or if we couldn't correlate
    if (reply.getNoOfTracks() == 0 || (random.nextInt() % randomChance) == 0) {
        // Just pick any random track that we don't already have
      ServerDatabase spares = getSpares(user);
      Track track = spares.randomTrack(random);
      if (track != null) {
        System.out.println("Random: " + track.getName());
        reply.add(track);
      }
    }

      // If we couldn't find any tracks to add then show a dialog.
    if (reply.getNoOfTracks() == 0)
      reply.setError("empty", "empty.html");
    
      // Save the user database
    purge(reply);
    try {
      user.add(reply);
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
    for (int i = 0; i < tracks.length; i++) {
      Track track = tracks[i];
      track.unSetRating();
      track.unSetNoOfTimesPlayed();
      track.unSetFile();
      track.unSetWeight();
    }
  }
}
