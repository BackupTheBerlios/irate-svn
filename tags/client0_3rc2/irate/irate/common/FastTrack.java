package irate.common;

import java.io.File;
import java.lang.ref.SoftReference;

import nanoxml.XMLElement;

public class FastTrack extends Track {
  
  private String key;
  private float rating;
  private SoftReference cacheDate;
  
  public FastTrack(XMLElement elt, File dir) {
    super(elt, dir);
    key = super.getKey();
    rating = super.getRawRating();
  }

  public FastTrack(Track track) {
    super(track);
    key = super.getKey();
    rating = super.getRawRating();
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

  public void incNoOfTimesPlayed() {
    cacheDate = null;
    super.incNoOfTimesPlayed();
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
