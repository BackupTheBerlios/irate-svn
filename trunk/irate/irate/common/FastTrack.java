package irate.common;

import java.net.URL;
import java.io.File;
import nanoxml.XMLElement;

public class FastTrack extends Track {
  
  private URL url;
  private float rating;
  
  public FastTrack(XMLElement elt, File dir) {
    super(elt, dir);
    url = super.getURL();
    rating = super.getRawRating();
  }
  
  public URL getURL() {
    return url;
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
