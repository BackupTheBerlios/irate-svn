// Copyright 2003 Anthony Jones, Stephen Blackheath, Taras

package irate.common;

import java.io.*;
import java.net.*;
import java.util.*;

import nanoxml.XMLElement;

public class Track {

  private static final TimeZone UTC = new SimpleTimeZone(/*SimpleTimeZone.UTC_TIME*/ 2, "UTC");
  private final int DEFAULT_RATING = 6;
  
  private XMLElement elt;
  private File dir;
  private TrackDatabase trackDatabase;

  public Track(XMLElement elt, File dir) {
    this.elt = elt;
    this.dir = dir;
  }
  
  public Track(Track track) {
    elt = new XMLElement(new Properties(), true, false);
    elt.setName("Track");
    copy(track);
  }
  
  private void copy(Track track) {
    dir = track.dir;
    setURL(track.getURL());
    setTitle(track.getTitle());
    setArtist(track.getArtist());
    setRating(track.getRawRating());
    setWeight(track.getWeight());
//    System.out.println(elt);
//    setWebURL(track.getWebURL());
    // copy other attributes
  }

  public void setTrackDatabase(TrackDatabase trackDatabase) {
    this.trackDatabase = trackDatabase;
  }

  public void updateSerial() {
    if(trackDatabase == null)
      return;

    elt.setAttribute("serial", ""+(trackDatabase.getSerial()+1));
  }

  public int getSerial() {
    String str = elt.getStringAttribute("serial");
    if(str == null)
      return 0;
    return Integer.parseInt(str);
  }
  
  public void setDownloadDir(File dir) {
    this.dir = dir;
  }

  /*package visibility*/ void setElement(XMLElement elt) {
    this.elt = elt;
  }
  
  public String toString() {
    String ratingStr = getState();
    String rating = " (" + ratingStr + "/" + getNoOfTimesPlayed()+ ")";
    String s = getName() + rating;
    if (getFile() == null)
      return "[" + s + "]";
    return s;
  }

  public String getName() {
    String artist = getArtist();
    String title = getTitle();
    if (artist.length() == 0) { 
      if (title.length() == 0)
        return "?";
      return title;
    }
    if (title.length() == 0)
      return artist;
    return artist + " / " + title;
  }

  /**
   * Return the rating with Float.NaN meaning that it hasn't been rated.
   */
  protected float getRawRating() {
    try {
      String s = elt.getStringAttribute("rating");
      if (s != null)
        return Float.parseFloat(s);
    }
    catch (NumberFormatException e) {
    }
    return Float.NaN;
  }

  public boolean isRated() {
    return !Float.isNaN(getRawRating());
  }

  public float getRating() {
    return getRating(DEFAULT_RATING);
  }

  public float getRating(float defaultRating) {
    if (isRated())
      return getRawRating();
    return defaultRating;
  }

  public void erase() {
    if (!isErased()) {
      File f = getFile();
      if (f != null && f.exists()) {
        try {
          f.delete();
        } 
        catch (Exception e) {
          e.printStackTrace();
        }
        setFileState("erased");
        elt.setAttribute("file", "");
      }
    }
  }
  
  public boolean isErased() {
    return getFileState().equals("erased");
  }

  public int getNoOfTimesPlayed() {
    try {
      String s = elt.getStringAttribute("played");
      if (s == null)
        s = "";
      return Integer.parseInt(s);
    }
    catch (NumberFormatException e) {
    }
    return 0;
  }
  
  private static String format(int i, int length) {
    String s = Integer.toString(i);
    while (s.length() < length) 
      s = "0" + s;
    return s;
  }
  
  public void incNoOfTimesPlayed() {
    synchronized (this) {
      elt.setAttribute("played", Integer.toString(getNoOfTimesPlayed() + 1));
      updateSerial();
      try {
        Calendar c = new GregorianCalendar(UTC);
        elt.setAttribute("last",
          format(c.get(Calendar.YEAR), 4) +
          format(c.get(Calendar.MONTH) + 1, 2) +
          format(c.get(Calendar.DAY_OF_MONTH), 2) +
          format(c.get(Calendar.HOUR_OF_DAY), 2) +
          format(c.get(Calendar.MINUTE), 2) +
          format(c.get(Calendar.SECOND), 2)
        );
      } 
      catch(Exception e) {
        e.printStackTrace();
      } 
    }
  }

  public void unSetNoOfTimesPlayed() {
    synchronized (this) {
      elt.removeAttribute("played");
      elt.removeAttribute("last");
    }
  }

  public String getLastPlayed() {
    String s = elt.getStringAttribute("last");
    if (s == null || s.indexOf(':') >= 0 || s.indexOf('/') >= 0 || s.indexOf('-') >= 0)
      return "";
    return s;
  }

  public void setRating(float rating) {
    if (Float.isNaN(rating))
      unSetRating();
    else
      elt.setAttribute("rating", Float.toString(rating));
    updateSerial();
  }

  public void unSetRating() {
    elt.removeAttribute("rating");
  }

  public void setWeight(float weight) {
    if (Float.isNaN(weight))
      unSetWeight();
    else
      elt.setAttribute("weight", Float.toString(weight));
    updateSerial();
  }

  public void unSetWeight() {
    elt.removeAttribute("weight");
  }

  public float getWeight() {
    try {
      String s = elt.getStringAttribute("weight");
      if (s != null)
        return Float.parseFloat(s);
    }
    catch (Exception e) {
    }
    return Float.NaN;
  }

  public void setVolume(int volume) {
    elt.setAttribute("volume", Integer.toString(volume));
  }

  public int getVolume() {
    try {
      return Integer.parseInt(elt.getStringAttribute("volume"));
    }
    catch (Exception e) {
    }
    return 0;
  }
  
  public void unSetVolume() {
    elt.removeAttribute("volume");
  }
  
  public void setBroken() {
    setFileState("broken");
  }

  private String getFileState() {
    String s = elt.getStringAttribute("state");
    if (s == null)
      return "";
    return s;
  }

  private void setFileState(String state) {
    elt.setAttribute("state", state);
  }

  public boolean isBroken() {
    if (getFileState().equals("broken"))
      return true;

      // This part is for backwards compatibility
    String s = elt.getStringAttribute("broken");
    return s != null && (s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("true"));
  }

  public boolean isHidden() {
    return isBroken() || getRating() == 0;
  }

  public void setArtist(String artist) {
    elt.setAttribute("artist", artist);
  }

  public String getArtist() {
    String artist = elt.getStringAttribute("artist");
    if (artist == null)
      return "";
    return artist;
  }
  
  public void setTitle(String title) {
    elt.setAttribute("title", title);
  }

  public String getTitle() {
    String title = elt.getStringAttribute("title");
    if (title == null)
      return "";
    return title;
  }

  public void setURL(URL url) {
    elt.setAttribute("url", url.toString());
  }
  
  public URL getURL() {
    try {
      String url = elt.getStringAttribute("url");
      if (url == null)
        url = "";
      return new URL(url);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public String getKey() {
    String key = elt.getStringAttribute("url");
    if (key == null)
      return "";
    return key;
  }

  public File getFile() {
    String filename = elt.getStringAttribute("file");
    if (filename == null || filename.length() == 0) {
      URL url = getURL();
      if (url.getProtocol().equals("file"))
        return new File(url.getFile());
      return null;
    }
    String separator = System.getProperties().getProperty("file.separator");
    int index = filename.lastIndexOf(separator);
    if (index >= 0)
      filename = filename.substring(index + 1);
    return new File(dir, filename); 
  }

  public void setFile(File file) {
    elt.setAttribute("file", file.getPath());
  }

  public void unSetFile() {
    elt.removeAttribute("file");
  }
  
  public boolean isOnPlayList() {
    float rating = getRating();
    return rating != 0 && (getNoOfTimesPlayed() % rating) != 0;      
  }

  public float getProbability() {
    int noOfTimesPlayed = getNoOfTimesPlayed();
    float rating = getRating(1);
    float prob = /* rating * */ rating / (1 + noOfTimesPlayed);
    if (prob < 0)
      return 0;

      // Make it 1/1000th of the chance of being played if the number of times 
      // played is a multiple of the rating. This means that a track will get
      // played in chunks proportional to the number of times it has been 
      // played.
    if (isOnPlayList())
      prob *= 1000;
      
    return prob;
  }

  public String getState() {
    if (isBroken()) 
      return "Broken";
    File file = getFile();
    if (file == null)
      return "Not downloaded";
    if (!file.exists())
      return "Missing";
    if (isRated())
      return Integer.toString((int) getRating());
    return "Unrated";
  }
  
  public XMLElement getElement() {
    return elt;
  }

  public int compareTo(Track track) {
    String a0 = getArtist();
    String a1 = track.getArtist();
    int comp = a0.compareToIgnoreCase(a1);
    if (comp != 0)
      return comp;
    return getTitle().compareToIgnoreCase(track.getTitle());
  }
  
  public boolean equals(Track track) {
    return getURL().equals(track.getURL());
  }

  public int hashCode()
  {
    return getURL().hashCode();
  }
}
