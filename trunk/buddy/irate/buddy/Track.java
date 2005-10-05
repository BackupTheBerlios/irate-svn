package irate.buddy;

import java.io.Serializable;

public class Track implements Serializable {

  public Track(String url, String artist, String title) {
    this.url = url;
    this.artist = artist;
    this.title = title;
  }

  public String url;

  public String artist;

  public String title;
}
