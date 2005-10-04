package irate.buddy;

import java.io.Serializable;

public class Track implements Serializable {

  public transient UniqueId trackId;
  
  public String url;
  
  public String artist;

  public String title;
}
