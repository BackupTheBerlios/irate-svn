/*
 * Created on Feb 21, 2004
 */
package irate.common;

import java.net.URL;

/**
 * @author Anthony Jones
 */
public interface TrackDetails {
  public String getArtist();
  public String getTitle();
  public URL getURL();
  public URL getWebSite();
  public URL getLicense();
}
