package irate.common;

import java.io.File;
import java.lang.ref.SoftReference;

import nanoxml.XMLElement;

public class FastTrack extends Track {
  
  private String key;
  private float rating;
  private int noOfTimesPlayed;
  private SoftReference cacheDate;
  private SoftReference cacheArtist;
  private SoftReference cacheTitle;
  
  public FastTrack(XMLElement elt, File dir) {
    super(elt, dir);
    key = super.getKey();
    rating = super.getRawRating();
    noOfTimesPlayed = super.getNoOfTimesPlayed();
  }

  public FastTrack(Track track) {
    super(track);
    key = super.getKey();
    rating = super.getRawRating();
    noOfTimesPlayed = super.getNoOfTimesPlayed();
  }
  
  public String key() {
    return key;
  }
  
  public float getRawRating() {
    return rating;
  }

  public void setRating(float rating) {
    super.setRating(rating);
    this.rating = rating;
  }
  
  public void unSetRating() {
    super.unSetRating();
    this.rating = Float.NaN;
  }
  
  public int getNoOfTimesPlayed() {
    return noOfTimesPlayed;
  }
  
  public void incNoOfTimesPlayed() {
    super.incNoOfTimesPlayed();
    noOfTimesPlayed = super.getNoOfTimesPlayed();
  }

  public void unSetNoOfTimesPlayed() {
    super.unSetNoOfTimesPlayed();
    noOfTimesPlayed = super.getNoOfTimesPlayed();
  }

  public void setArtist(String artist) {
    cacheArtist = new SoftReference(artist);
    super.setArtist(artist);
  }
  
  public String getArtist() {
    String artist = cacheArtist == null ? null : (String) cacheArtist.get(); 
    if (artist == null) {
      artist = super.getArtist();
      cacheArtist = new SoftReference(artist);
    }
    return artist;
  }

  public void setTitle(String title) {
    cacheTitle = new SoftReference(title);
    super.setTitle(title);
  }
  
  public String getTitle() {
    String title = cacheTitle == null ? null : (String) cacheTitle.get(); 
    if (title == null) {
      title = super.getTitle();
      cacheTitle = new SoftReference(title);
    }
    return title;
  }

  public void updateTimeStamp() {
    cacheDate = null;
    super.updateTimeStamp();
  }

  public Date getLastPlayed() {
    Date date = cacheDate == null ? null : (Date) cacheDate.get(); 
    if (date == null) {
      date = super.getLastPlayed();
      cacheDate = new SoftReference(date);
    }
    return date;
  }
}
