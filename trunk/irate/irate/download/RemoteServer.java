/*
 * Created on 18/07/2005
 */
package irate.download;

import irate.common.TrackDatabase;


public interface RemoteServer {

  /**
   * Connects to the server, uploads the trackdatabase and requests new tracks
   * to download.
   * 
   * @param trackDatabase
   *          the trackdatabase that we work with.
   */
  public void contactServer(TrackDatabase trackDatabase) throws DownloadException;
  
}
