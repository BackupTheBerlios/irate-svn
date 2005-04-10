/*
 * Created on Apr 13, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package irate.download;

import irate.common.Track;

/**
 * @author taras
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface DownloadListener {

  public void downloadStarted(Track track);

  public void downloadProgressed(Track track, int percentComplete, String state);

  public void downloadData(Track track, byte[] buffer, int offset, int length);

  public void downloadFinished(Track track, boolean succeeded);
}
