// Copyright 2003 Anthony Jones

package irate.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import nanoxml.XMLElement;

import irate.common.Track;
import irate.common.TrackDatabase;

public class ServerDatabase extends TrackDatabase {
  
  public static final int noOfFriendsToRecord = 100;
  
  private final int ratingScale = 1000;
  private Set friends;
  private UserList userList;
  
  public ServerDatabase() {
  }
  
  public ServerDatabase(UserList userList, File file) throws IOException {
    super(file);
    this.userList = userList;
  }

  public ServerDatabase(UserList userList, InputStream is) throws IOException {
    super(is);
    this.userList = userList;
  }
  
  protected void load() {
    if (friends == null) {
      friends = new HashSet();
      XMLElement docElt = getDocElement();
      Enumeration e = docElt.enumerateChildren();
      while(e.hasMoreElements()) {
        XMLElement elt = (XMLElement)e.nextElement();
        if (elt.getName().equals("Friend")) {
          DatabaseReference friend = userList.getUser(elt);
          friends.add(friend);
        }
      }
    }
  }

  public int getProbability(Track track) {
    if (track.isRated()) {
      float rating = track.getRating();
      
        // This will mean that a track is only recommended to a user if the
        // rating is high enough.
      if (rating >= 6.0F) {
        float prob = rating * rating;
        float weight = track.getWeight();
        if (!Float.isNaN(weight))
          return Math.round(prob * weight * 1000);
        return Math.round(prob * 1000);
      }
    }
    return 0;
  }

  public Track randomTrack(Random random) {
    Track[] tracks = getTracks();
    if (tracks.length == 0) 
      return null; 
    return tracks[(random.nextInt() & 0x7fffffff) % tracks.length];
  }

  public void addFriend(DatabaseReference friend) {
    load();
    if (!friends.contains(friend)) {
      friends.add(friend);
      getDocElement().addChild(friend.getElement());
    }
  }

  public void removeFriend(DatabaseReference friend) {
    load();
    if (friends.contains(friend)) {
      friends.remove(friend);
      getDocElement().removeChild(friend.getElement());
    }
  }

  public void setFriendSet(Set newFriends) {
    XMLElement docElt = getDocElement();
    Enumeration enum = docElt.enumerateChildren();
    Vector v = new Vector();
    while(enum.hasMoreElements()) {      
      XMLElement elt = (XMLElement)enum.nextElement();
      if (elt.getName().equals("Friend"))
        v.add(elt);
    }
    for (Iterator itr = v.iterator(); itr.hasNext(); )
      docElt.removeChild((XMLElement) itr.next());
      
    friends = new HashSet(newFriends);
    for (Iterator itr = friends.iterator(); itr.hasNext(); ) {
      DatabaseReference friend = (DatabaseReference) itr.next();
      getDocElement().addChild(friend.getElement());
    }
//    System.out.println("Friend size:: " + friends.size());
  }
  
  public Set getFriendSet() {
    load();
    return friends;
  }  
}
