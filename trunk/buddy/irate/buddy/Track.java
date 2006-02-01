package irate.buddy;

import java.io.Serializable;

public class Track implements Serializable {

  public Track(String url, String artist, String title, String www) {
    this.url = url;
    this.artist = artist;
    this.title = title;
    this.www = www;
  }

  public String url;

  public String artist;

  public String title;
  
  public String www;
}
