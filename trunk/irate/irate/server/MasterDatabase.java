// Copyright 2003 Anthony Jones

package irate.server;

import irate.common.*;

import java.io.*;
import java.util.*;

public class MasterDatabase extends ServerDatabase {

    /** Add an orphan track one time in n. */
  private final int orphanChance = 5;

    /** Add a random track one time in n. */
  private final int randomChance = 3;

    /** Issue five tracks to correlate. */
  private final int correlateNoOfTracks = 4;

    /** The number of peer tracks to start with. */
  private final int initialPeerTracks = 8;

    /** The number of peer tracks to download before switching to random. */
  private final int peerThreshhold = 14;

    /** The number of times it will pick a user in order to get a random track. */
  private final int peerRetries = 30;
  
    /** The numebr of users to compare each user to. */
  private final int noOfUsersToCompare = ServerDatabase.noOfFriendsToRecord * 3;
  
  private UserList userList;
  private Random random = new Random();
  private ServerDatabase orphans;
  private Track[] compulsoryTracks;

    // The user rate is the number of songs the database must have for each
    // track the user is allowed.
  private int userRate = 5;
  
  public MasterDatabase(File file, UserList userList) throws IOException {
    super(userList, file);
    this.userList = userList;
    
      // Find the compulsory tracks
    ServerDatabase compulsory = getCompulsory();
    compulsoryTracks = compulsory.getTracks();

      // Find the orphans
    System.out.println("Finding orphans");
//    orphans = findOrphans();
//    System.out.println(orphans.getNoOfTracks() + " orphans");
    userList.discardAll();
  }
  
  public ServerDatabase processRequest(ServerDatabase request) {
    ServerDatabase reply = null; 
    try {
      reply = doProcessRequest(request);
    }
    finally {
      userList.discardAll();
    }
    return reply;
  }
  
  private ServerDatabase doProcessRequest(ServerDatabase request) {
    ServerDatabase reply = new ServerDatabase();

    System.out.println("User " + request.getUserName() + " " + request.getNoOfTracks());
    DatabaseReference userRef = userList.getUser(request.getUserName());
    ServerDatabase user = null;
    try {
      if (userRef == null)
        userRef = userList.createUser(request.getUserName(), request.getPassword());
      user = userRef.getServerDatabase();
    }
    catch (IOException e) {
      e.printStackTrace();
      reply.setError("user", "user.html");
      return reply;      
    }

      // If the user doesn't exist or the password is incorrect the return a 
      // blank response.
    if (user == null) {
        // If the user already has tracks
      if (request.getNoOfTracks() != 0 || request.getUserName().length() == 0 || request.getPassword().length() == 0) {
        reply.setError("user", "user.html");
        return reply;
      }
      try {
        DatabaseReference ref = userList.createUser(request.getUserName(), request.getPassword());
      }
      catch (IOException e) {
        reply.setError("user", "user.html");
        return reply;
      }
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

    if (!user.hasRatedEnoughTracks()) {
      reply.setError("notenoughratings", "http://www.irateradio.org/irate/client/help/notenoughratings.html");
      return reply;
    }

    for (int i = 0; i < compulsoryTracks.length; i++) {
      Track track = compulsoryTracks[i];
      if (user.getTrack(track) == null) {
        System.out.println("Compulsory: " + track.getName());
        reply.add(track);
      }
    }

    if (user.getNoOfTracks() >= initialPeerTracks) {
/*
      if ((random.nextInt() % orphanChance) == 0) {
          // If there are any orphans then we add one here
        Track track = orphans.randomTrack(random);
        if (track != null) {
          System.out.println("Orphan: " + track.getName());
    
          reply.add(track);
        }
      }
*/

        // See if we can correlate a track
      ServerDatabase corel = getBest(user);
      System.out.println("Correlated tracks: " + corel.getNoOfTracks());
      for (int i = 0; i < correlateNoOfTracks; i++) {
        Track track = corel.chooseTrack(random);
        if (track != null) {
          System.out.println("Correlation: " + track.getName() + " " + track.getRating() + " " + (int) track.getWeight());
          reply.add(track);
          corel.remove(track);
        }
      }
    }

      // Correlate tracks for new users
    if (reply.getNoOfTracks() == 0 && user.getNoOfTracks() < peerThreshhold) {
      for (int i = 0; i < peerRetries; i++) {
        DatabaseReference peerRef = userList.randomUser(random, user);
        if (peerRef != null) {
          try {
            ServerDatabase peer = peerRef.getServerDatabase();
            System.out.println("Peer: " + peer.getUserName());
            Track track = peer.chooseTrack(random);
            if (track != null && user.getTrack(track) == null) {
              System.out.println("Peer track: " + track.getName() + " " + track.getRating());
              reply.add(track);
              break;
            }
          }
          catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }
        
      // Do this randomly or if we couldn't correlate
    if (reply.getNoOfTracks() == 0 || (random.nextInt() % randomChance) == 0) {
      Track track = randomTrack(random);
      if (track == null || user.getTrack(track) != null) {
          // Just pick any random track that we don't already have
        ServerDatabase spares = getSpares(user);
        track = spares.randomTrack(random);
      }
      if (track != null) {
        System.out.println("Random: " + track.getName());
        reply.add(track);
      }
    }

      // Search for tracks which the user hasn't downloaded
    {
      Track[] tracks = user.getTracks();
      for (int i = 0; i < tracks.length; i++) {
        Track track = tracks[i];
        if (!track.isRated() && track.getFile() != null && !track.isBroken())
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
      userRef.save();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    
    return reply;
  } 
  
  public ServerDatabase findOrphans() {
    ServerDatabase orphans = new ServerDatabase();
    Track[] tracks = getTracks();
    DatabaseReference[] users = userList.getUsers();
    ServerDatabase[] sd = new ServerDatabase[users.length];
    for (int j = 0; j < users.length; j++) 
      try {
        sd[j] = users[j].getServerDatabase();
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    for (int i = 0; i < tracks.length; i++) {
      add: {
        for (int j = 0; j < sd.length; j++)
          if (sd[j] != null && sd[j].getTrack(tracks[i]) != null)
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
    Set users = new HashSet();
    users.addAll(user.getFriendSet());
    System.out.println("No. of friends: " + users.size());
    users.addAll(userList.getRandomUserSet(random, user, noOfUsersToCompare - users.size()));
    
    TrackAverageRating tar = new TrackAverageRating();
    TreeMap treeMap = new TreeMap(new Comparator() {
      public int compare(Object o0, Object o1) {
        DatabaseCorrelator dc0 = (DatabaseCorrelator) o0;
        DatabaseCorrelator dc1 = (DatabaseCorrelator) o1;
        if (dc0.getCorrelation() > dc1.getCorrelation())
          return -1;
        return 1;
      }
    });
    for (Iterator itr = users.iterator(); itr.hasNext(); ) {
      try {
        DatabaseReference peer = (DatabaseReference) itr.next();
        DatabaseCorrelator dc = new DatabaseCorrelator(user, peer);
        dc.process();
        float correlation = dc.getCorrelation();
        if (correlation > 0) {
//          System.out.println("Friend: " + users[i].getUserName() + " " + dc.getCorrelation() + " " + dc.getSpares().getNoOfTracks());
          treeMap.put(dc, dc);
        }
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }
    Set friends = new HashSet();
    int count = noOfFriendsToRecord;
    for (Iterator itr = treeMap.values().iterator(); count > 0 && itr.hasNext(); count--) {
      DatabaseCorrelator dc = (DatabaseCorrelator) itr.next();
      friends.add(dc.getDatabaseReference());
      tar.add(dc.getSpares(), dc.getCorrelation());
//      if (dc.getSpares().getNoOfTracks() != 0)
//        System.out.println("Friend: " + dc.getCorrelation() + " " + dc.getSpares().getNoOfTracks());
    }
    System.out.println("Saving friends: " + friends.size());
    user.setFriendSet(friends);
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
      track.unSetVolume();
    }
  }

  public ServerDatabase getCompulsory() {
    System.out.println("Finding compulsory tracks");
    ServerDatabase comp = new ServerDatabase();
    Track[] tracks = getTracks();
    for (int i = 0; i < tracks.length; i++) {
      Track track = tracks[i];
      if (!Float.isNaN(track.getWeight())) {
        System.out.println(track.getName());
        comp.add(track);
        track.unSetWeight();
      }
    }
    return comp;
  }
}
