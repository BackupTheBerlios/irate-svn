package irate.common;

import java.io.*;
import java.net.*;
import org.w3c.dom.*;

public class Track {

  private final int DEFAULT_RATING = 5;
  
  private Element elt;
  
  public Track(Element elt) {
    this.elt = elt;
  }

  public String toString() {
    String ratingStr = isRated() ? Integer.toString((int) getRating()) : "UNRATED";
    String rating = " (" + ratingStr + "/" + getNoOfTimesPlayed()+ ") ";
    return getName() + rating;
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
      return Float.parseFloat(elt.getAttribute("rating"));
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
    return DEFAULT_RATING;
  }

  public int getNoOfTimesPlayed() {
    try {
      return Integer.parseInt(elt.getAttribute("played"));
    }
    catch (NumberFormatException e) {
    }
    return 0;
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
      return Integer.parseInt(elt.getAttribute("weight"));
    }
    catch (NumberFormatException e) {
    }
    return Float.NaN;
  }

  public void incNoOfTimesPlayed() {
    synchronized (this) {
      elt.setAttribute("played", Integer.toString(getNoOfTimesPlayed() + 1));
    }
  }

  public String getArtist() {
    return elt.getAttribute("artist");
  }

  public String getTitle() {
    return elt.getAttribute("title");
  }

  public URL getURL() {
    try {
      return new URL(elt.getAttribute("url"));
    }
    catch (MalformedURLException e) {
      e.printStackTrace();
    }
    return null;
  }

  public String getKey() {
    return elt.getAttribute("url");
  }

  public File getFile() {
    String filename = elt.getAttribute("file");
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
    float prob = getRating() / (1 + getNoOfTimesPlayed());
    if (prob < 0)
      return 0;
    return prob;
  }

  public Element getElement() {
    return elt;
  }

  public boolean equals(Track track) {
    return getURL().equals(track.getURL());
  }
}
