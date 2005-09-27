/*
 * Created on 18/07/2005
 */
package irate.download;

import irate.common.Track;
import irate.common.TrackDatabase;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

public class RemoteBuddyServer implements RemoteServer {

  static URL defaultUrl;
  static {
    try {
      defaultUrl = new URL("http://127.0.0.1:8031/");
    }
    catch (MalformedURLException e) {
      e.printStackTrace();
    }
  }

  private XmlRpcClient client;

  public static void main(String args[]) {
    try {
      TrackDatabase trackDatabase = new TrackDatabase();
      new RemoteBuddyServer().contactServer(trackDatabase);
    }
    catch (DownloadException e) {
      e.printStackTrace();
    }
  }

  public RemoteBuddyServer() {
    this(defaultUrl);
  }

  public RemoteBuddyServer(URL url) {
    client = new XmlRpcClient(url);
  }

  public void contactServer(TrackDatabase trackDatabase)
      throws DownloadException {
    try {
      Hashtable hashTable = convertTracksToHashtable(trackDatabase.getTracks());

      Vector args = new Vector();
      args.add(hashTable);
      
      System.out.print("Sending ratings to server...");
      client.execute("Buddy.setRatings", args);
      System.out.println(" done");
    }
    catch (IOException ioe) {
      ioe.printStackTrace();
      throw new DownloadException();
    }
    catch (XmlRpcException mue) {
      mue.printStackTrace();
      throw new DownloadException();
    }
  }

  private Hashtable convertTracksToHashtable(Track[] tracks) {
    Hashtable hashTable = new Hashtable();
    for (int i = 0; i < tracks.length; i++) {
      Track track = tracks[i];
      hashTable.put(track.getKey(), new Float(track.getRating()));
    }
    return hashTable;
  }
}
