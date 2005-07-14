/*
 * Created on 14/07/2005
 */
package irate.download;

import irate.common.Track;
import irate.common.TrackDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.List;


public class TrackDownloader extends Thread {

  private TracksBeingDownloaded tracksBeingDownloaded;

  private List downloadListeners;

  private TrackDatabase trackDatabase;

  private ExponentialBackoffManager exponentialBackoffManager;

  private Track track;

  private DownloadThread dt;

  public TrackDownloader(TracksBeingDownloaded tracksBeingDownloaded,
      List downloadListeners, TrackDatabase trackDatabase,
      ExponentialBackoffManager exponentialBackoffManager, Track track) {
    this.tracksBeingDownloaded = tracksBeingDownloaded;
    this.downloadListeners = downloadListeners;
    this.trackDatabase = trackDatabase;
    this.exponentialBackoffManager = exponentialBackoffManager;
    this.track = track;
    setDaemon(true); // This means the JVM is permitted to exit in the middle
    // of a transfer
  }

  public void run() {
    try {
      download();
    }
    catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  /**
   * Get the file name associated with the given URL.
   */
  private File getFileName(URL url) {
    String urlString = url.toString();
    int index = urlString.lastIndexOf('/');
    if (index > 0) urlString = urlString.substring(index + 1);
    urlString = URLDecoder.decode(urlString); //$NON-NLS-1$
    return new File(trackDatabase.getDownloadDirectory(), urlString);
  }

  /**
   * Create URL which contains any appropriate proxy information.
   */
  private URL getProxyURL(URL url) throws MalformedURLException {
    String proxy = trackDatabase.getHTTPProxy(); // JDR
    if ((proxy != null) && (!proxy.trim().equals(""))) // JDR //$NON-NLS-1$
    {// JDR HTTP proxy *is* defined in xml file.
      // System.out.println("\nUsing HTTPProxy:"+HTTPProxy+"\n"); //JDR
      return new URL("http", proxy, trackDatabase.getHTTPProxyPort(), url
          .toString());// JDR
      // //$NON-NLS-1$
      // JDR The URL constructor created this way will cause the URL to go
      // through the proxy.
      // JDR port -1 = protocol port default (example http uses port 80 unless
      // otherwise specified).
    }
    return url;
  }

  public void download() throws IOException {
    int percentComplete = -1;
    tracksBeingDownloaded.add(track);

    final URL url = getProxyURL(track.getURL());

    System.out.println(url);
    final File finishedFile = getFileName(url);
    final File downloadingFile = new File(finishedFile.toString() + ".part");
    if (finishedFile.exists()) {
      // We've already have the finished file then we rename it to .part in
      // case it's a partially downloaded file from a previous version of
      // the program.
      if (!finishedFile.renameTo(downloadingFile)) {
        System.out.println("Failed to rename " + finishedFile + " to "
            + downloadingFile);
        finishedFile.delete();
      }
    }

    try {
      long timeout = 120000;
      DownloadConnection connection = new DownloadConnection(url);
      connection.connect(0, timeout);
      // Get rid of the problem where tracks are downloaded but in reality
      // they are 404 messages or some other html crud.
      String contentType = connection.getContentType();
      if (contentType == null)
        throw new ProtocolException("Something went wrong talking to the "
            + "download site");
      if (contentType.indexOf("text") != -1)
        throw new FileNotFoundException("Content type is Text");
      int contentLength = connection.getContentLength();
      long continueOffset = downloadingFile.exists() ? downloadingFile
          .length() : 0;
      // If the file isn't already the right length, then we need to download
      if (continueOffset != contentLength) {
        boolean resume = false;
        if (continueOffset > 0) {
          resume = true;
          DownloadConnection resumeConnection = null;
          try {
            resumeConnection = new DownloadConnection(url);
            resumeConnection.connect(continueOffset, timeout);
            System.out.println("Resuming download of " + track + " (offset="
                + continueOffset + ")");
          }
          catch (DownloadConnection.ResumeNotSupportedException exception) {
            System.out.println("Server does not support resuming downloads");
            resume = false;
            continueOffset = 0;
          }
          if (resume) {
            try {
              connection.close(timeout);
            }
            catch (IOException e) {
            }
            connection = resumeConnection;
          }
        }
        for (int i = 0; i < downloadListeners.size(); i++)
          ((DownloadListener) downloadListeners.get(i))
              .downloadStarted(track);
        final byte buf[] = new byte[8192];

        // If we are resuming a download, then we must feed the
        // already-downloaded
        // part of the file to the download listeners. They are only expected
        // to deal with whole files.
        if (resume && downloadListeners.size() != 0) {
          FileInputStream is = new FileInputStream(downloadingFile.toString());
          try {
            while (true) {
              int nbytes = is.read(buf, 0, buf.length);
              if (nbytes <= 0) break;
              for (int i = 0; i < downloadListeners.size(); i++)
                ((DownloadListener) downloadListeners.get(i)).downloadData(
                    track, buf, 0, nbytes);
            }
          }
          finally {
            is.close();
          }
        }

        // If the continue offset is non-zero then we open the file in
        // append mode. If the file on the server is shorter than the one
        // we have on disk then we just start again.
        OutputStream os = new FileOutputStream(downloadingFile.toString(),
            resume);
        boolean succeeded = false;
        try {
          int totalBytes = (int) continueOffset;
          while (true) {
            int nbytes = connection.read(buf, timeout);
            if (nbytes < 0) break;
            os.write(buf, 0, nbytes);
            // Stephen Blackheath: I am in two minds as to whether this should
            // be a separate
            // listener, but it should be efficient enough as it is.
            for (int i = 0; i < downloadListeners.size(); i++)
              ((DownloadListener) downloadListeners.get(i)).downloadData(
                  track, buf, 0, nbytes);
            if (contentLength >= 0) {
              totalBytes += nbytes;
              int percent = totalBytes * 100 / contentLength;
              if (percent != percentComplete) {
                percentComplete = percent;
                if (track.setPercentComplete(percent)) {
                  for (Iterator iter = downloadListeners.iterator(); iter
                      .hasNext();) {
                    DownloadListener d = (DownloadListener) iter.next();
                    d.downloadProgressed(track, percent, Resources
                        .getString("DownloadThread.Downloading_Tracks"));
                  }
                }
              }
            }
          }
          os.close();
          os = null;
          if (downloadingFile.renameTo(finishedFile)) {
            track.setFile(finishedFile);
            succeeded = true;
            System.out.println("Finished downloading to " + finishedFile);
          }
          else {
            // I don't think this could occur, since we've already
            // checked to see if finishedFile exists, but...
            System.out.println("For some reason we couldn't rename \""
                + downloadingFile + "\" to \"" + finishedFile + "\"");
          }
          exponentialBackoffManager.succeeded(track.getURL());
        }
        finally {
          if (os != null) os.close();

          try {
            connection.close(timeout);
          }
          catch (IOException exception) {
          }
          for (int i = 0; i < downloadListeners.size(); i++)
            ((DownloadListener) downloadListeners.get(i)).downloadFinished(
                track, succeeded);
        }
      }
      trackDatabase.save();
    }
    catch (Exception e) {
      e.printStackTrace();
      if (e instanceof FileNotFoundException) {
        track.setBroken();
      }
      else if (e instanceof IOException) {
        exponentialBackoffManager.failed(track.getURL());
        track.increaseDownloadAttempts();
      }
      else {
        System.out.println("Exception not handled");
      }
    }
    tracksBeingDownloaded.remove(track);
  }

}
