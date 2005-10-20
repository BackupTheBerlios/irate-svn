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
//      Vector args = new Vector();
//      Object response = (Object) client.execute("Session.ping", args);

      System.out.print("Logging in...");
      String sessionId = login(trackDatabase.getUserName(), trackDatabase
          .getPassword(), true);
      System.out.println(" done");

      System.out.print("Sending ratings...");
      setRatings(sessionId, trackDatabase.getTracks());
      System.out.println(" done");
      
      System.out.print("Fetching list...");
      Track[] newTracks = fetchTracks(sessionId, trackDatabase.getTracks());      
      System.out.println(" done");
      System.out.println("No of tracks: " + newTracks.length);
      
      System.out.print("Fetching track details...");
      fetchTrackDetails(sessionId, newTracks);
      System.out.println(" done");
      
      // add new tracks to track database
      
      System.out.print("Logging out...");
      logout(sessionId);
      System.out.println(" done");
    }
    catch (IOException ioe) {
      ioe.printStackTrace();
      // throw new DownloadException();
    }
    catch (XmlRpcException mue) {
      mue.printStackTrace();
      // throw new DownloadException();
    }
  }
  
  private Hashtable convertTrackToHashTable(Track track) {
    Hashtable hashtable = new Hashtable();
    hashtable.put("url", track.getURL().toString());
    hashtable.put("rating", new Float(track.isRated() ? track.getRating() : Float.NaN));
    return hashtable;
  }

  private Vector convertTracksToVector(Track[] tracks) {
    Vector vector = new Vector();
    for (int i = 0; i < tracks.length; i++) {
      Track track = tracks[i];
      if (track.isRated())
        vector.add(convertTrackToHashTable(track));      
    }
    return vector;
  }

  public String login(String userName, String password, boolean create)
      throws XmlRpcException, IOException {
    Vector args = new Vector();
    args.add(userName);
    args.add(password);
    args.add(new Boolean(create));
    Object response = (Object) client.execute("Session.login", args);
    return (String) response;
  }

  public void logout(String sessionId) throws XmlRpcException, IOException {
    Vector args = new Vector();
    args.add(sessionId);
    client.execute("Session.logout", args);
  }

  private void setRatings(String sessionId, Track[] tracks)
      throws XmlRpcException, IOException {
    Vector args = new Vector();
    args.add(sessionId);
    args.add(convertTracksToVector(tracks));

    client.execute("Rating.setTrackData", args);
  }
  
  private Track[] fetchTracks(String sessionId, Track[] tracks) throws XmlRpcException, IOException {
	  Vector args = new Vector();
	  args.add(sessionId);
	  Object reponse = (Object) client.execute("Track.get", args);
	  return new Track[0];
  }
  
  private void fetchTrackDetails(String sessionId, Track[] tracks) throws XmlRpcException, IOException {
	  Vector args = new Vector();
	  args.add(sessionId);
	  Object reponse = (Object) client.execute("Track.get", args);
  }
}
