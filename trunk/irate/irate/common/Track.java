package irate.common;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import nanoxml.XMLElement;

public class Track {

  private final int DEFAULT_RATING = 7;
  private final int INITIAL_RATING = 10;
  
  private XMLElement elt;
  
  public Track(XMLElement elt) {
    this.elt = elt;
  }

  public String toString() {
    String ratingStr = isRated() ? Integer.toString((int) getRating()) : "UNRATED";
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
   * Return the rating with -1 meaning that it hasn't been rated.
   */
  private float getRawRating() {
    try {
      return Float.parseFloat(elt.getStringAttribute("rating"));
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

  public int getNoOfTimesPlayed() {
    try {
      return Integer.parseInt(elt.getStringAttribute("played"));
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
    return elt.getStringAttribute("last");
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
      return Integer.parseInt(elt.getStringAttribute("weight"));
    }
    catch (NumberFormatException e) {
    }
    return Float.NaN;
  }

  public void setBroken() {
    elt.setAttribute("broken", "yes");
  }

  public boolean isBroken() {
    String s = elt.getStringAttribute("broken");
    return s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("true");
  }

  public boolean isHidden() {
    return isBroken() || getRating() == 0;
  }

  public String getArtist() {
    return elt.getStringAttribute("artist");
  }

  public String getTitle() {
    return elt.getStringAttribute("title");
  }

  public URL getURL() {
    try {
      return new URL(elt.getStringAttribute("url"));
    }
    catch (MalformedURLException e) {
      e.printStackTrace();
    }
    return null;
  }

  public String getKey() {
    return elt.getStringAttribute("url");
  }

  public File getFile() {
    String filename = elt.getStringAttribute("file");
    if (filename.length() == 0) {
      URL url = getURL();
      if (url.getProtocol().equals("file"))
        return new File(url.getFile());
      return null;
    }
    return new File(filename); 
  }

  public void setFile(File file) {
    elt.setAttribute("file", file.getPath());
  }

  public void unSetFile() {
    elt.setAttribute("file", "");
  }

  public float getProbability() {
    float rating = getRating();
    float prob = rating * rating / (1 + getNoOfTimesPlayed());
    if (prob < 0)
      return 0;
    return prob;
  }

  public XMLElement getElement() {
    return elt;
  }

  public boolean equals(Track track) {
    return getURL().equals(track.getURL());
  }
}
