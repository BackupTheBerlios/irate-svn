package irate.common;

import java.io.*;
import java.net.*;
import org.w3c.dom.*;

public class Track {

  private Element elt;

  private final int DEFAULT_RATING = 5;
  
  public Track(Element elt) {
    this.elt = elt;
  }

  public String toString() {
    int ratingValue = getRawRating();
    String ratingStr = ratingValue < 0 ? "UNRATED" : Integer.toString(getRating());
    String rating = " (" + ratingStr + "/" + getNoOfTimesPlayed()+ ")";
    String artist = getArtist();
    String title = getTitle();
    if (artist.length() == 0) { 
      if (title.length() == 0)
        return "?" + rating;
      return title + rating;
    }
    if (title.length() == 0)
      return artist + rating;
    return artist + " / " + title + rating;
  }

  /**
   * Return the rating with -1 meaning that it hasn't been rated.
   */
  private int getRawRating() {
    try {
      return Integer.parseInt(elt.getAttribute("rating"));
    }
    catch (NumberFormatException e) {
    }
    return -1;
  }

  public int getRating() {
    int rating = getRawRating();
    if (rating < 0)
	return DEFAULT_RATING;
    else
	return rating;
  }

  public int getNoOfTimesPlayed() {
    try {
      return Integer.parseInt(elt.getAttribute("played"));
    }
    catch (NumberFormatException e) {
    }
    return 0;
  }

  public void setRating(int rating) {
    elt.setAttribute("rating", Integer.toString(rating));
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

  public int getProbability() {
    int prob = 1000000 * getRating() / (1 + getNoOfTimesPlayed());
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
