package irate.common;

import java.io.File;

import nanoxml.XMLElement;

public class FastTrack extends Track {
  
  private String key;
  private float rating;
  
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
}
