// Copyright 2003 Anthony Jones, Stephen Blackheath, Taras

package irate.common;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import nanoxml.XMLElement;

public class Track {

  private final int DEFAULT_RATING = 6;
  private final int INITIAL_RATING = 10;
  
  private XMLElement elt;
  private File dir;
  
  public Track(XMLElement elt, File dir) {
    this.elt = elt;
    this.dir = dir;
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
      if (s == null)
        s = "";
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
    if (isRated())
      return getRawRating();
    if (getNoOfTimesPlayed() == 0)
      return INITIAL_RATING;
    return DEFAULT_RATING;
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
  
  public void incNoOfTimesPlayed() {
    synchronized (this) {
      elt.setAttribute("played", Integer.toString(getNoOfTimesPlayed() + 1));
      elt.setAttribute("last", new SimpleDateFormat().format(new Date()));
    }
  }

  public void unSetNoOfTimesPlayed() {
    synchronized (this) {
      elt.setAttribute("played", "");
      elt.setAttribute("last", "");
    }
  }

  public String getLastPlayed() {
    String s = elt.getStringAttribute("last");
    if (s == null)
      return "";
    return s;
  }

  public void setRating(float rating) {
    elt.setAttribute("rating", Float.toString(rating));
  }

  public void unSetRating() {
    elt.setAttribute("rating", "");
  }

  public void setWeight(float weight) {
    elt.setAttribute("weight", Float.toString(weight));
  }

  public void unSetWeight() {
    elt.setAttribute("weight", "");
  }

  public float getWeight() {
    try {
      String s = elt.getStringAttribute("weight");
      if (s == null)
        s = "";
      return Integer.parseInt(s);
    }
    catch (NumberFormatException e) {
    }
    return Float.NaN;
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

  public String getArtist() {
    String artist = elt.getStringAttribute("artist");
    if (artist == null)
      return "";
    return artist;
  }

  public String getTitle() {
    String title = elt.getStringAttribute("title");
    if (title == null)
      return "";
    return title;
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
    elt.setAttribute("file", "");
  }
  
  public boolean isOnPlayList() {
    float rating = getRating();
    return rating != 0 && (getNoOfTimesPlayed() % rating) != 0;      
  }

  public float getProbability() {
    int noOfTimesPlayed = getNoOfTimesPlayed();
    float rating = getRating();
    float prob = rating * rating / (1 + noOfTimesPlayed);
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
