/*
 * Created on 18/07/2005
 */
package irate.download;

import irate.common.Track;
import irate.common.TrackDatabase;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

public class RemoteBuddyServer implements RemoteServer {

  static URL defaultUrl;
  static {
    try {
      defaultUrl = new URL("http://127.0.0.1:8031/");
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
  }

  private XmlRpcClient client;

  public static void main(String args[]) {
    try {
      TrackDatabase trackDatabase = new TrackDatabase();
      new RemoteBuddyServer().contactServer(trackDatabase);
    } catch (DownloadException e) {
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
      // Vector args = new Vector();
      // Object response = (Object) client.execute("Session.ping", args);

      System.out.print("Logging in...");
      String sessionId = login(trackDatabase.getUserName(), trackDatabase
          .getPassword(), true);
      System.out.println(" done");

      System.out.print("Sending ratings...");
      setRatings(sessionId, trackDatabase.getTracks());
      System.out.println(" done");

      System.out.print("Fetching list...");
      String[] newTracks = fetchTracks(sessionId, trackDatabase.getTracks());
      System.out.println(" done");
      System.out.println("No of tracks: " + newTracks.length);

      if (newTracks.length != 0) {
        System.out.print("Fetching track details...");
        Track[] tracks = fetchTrackDetails(sessionId, newTracks);
        System.out.println(" done");

        System.out.print("Updating track database...");
        updateTrackDatabase(trackDatabase, tracks);
        System.out.println(" done");
      }

      System.out.print("Logging out...");
      logout(sessionId);
      System.out.println(" done");
    } catch (IOException ioe) {
      ioe.printStackTrace();
      // throw new DownloadException();
    } catch (XmlRpcException mue) {
      mue.printStackTrace();
      // throw new DownloadException();
    }
  }

  private Hashtable convertTrackToHashTable(Track track) {
    Hashtable hashtable = new Hashtable();
    hashtable.put("url", track.getURL().toString());
    hashtable.put("rating", new Float(track.isRated() ? track.getRating()
        : Float.NaN));
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

  private String[] fetchTracks(String sessionId, Track[] tracks)
      throws XmlRpcException, IOException {
    Vector args = new Vector();
    args.add(sessionId);
    Object response = (Object) client.execute("Rating.getTracks", args);
    Vector newTracks = (Vector) response;

    Set trackIds = new HashSet();
    for (int i = 0; i < tracks.length; i++)
      trackIds.add(tracks[i].getId());

    List newTrackIds = new ArrayList();
    for (Iterator itr = newTracks.iterator(); itr.hasNext();) {
      Hashtable rating = (Hashtable) itr.next();
      String id = (String) rating.get("trackId");
      if (!trackIds.contains(id))
        newTrackIds.add(id);
    }

    return (String[]) newTrackIds.toArray(new String[newTrackIds.size()]);
  }

  private Track[] fetchTrackDetails(String sessionId, String[] trackIds)
      throws XmlRpcException, IOException {
    Vector args = new Vector();
    args.add(sessionId);
    args.add(new Vector(Arrays.asList(trackIds)));
    Object response = (Object) client.execute("Track.getDetails", args);
    if (response instanceof XmlRpcException)
      ((XmlRpcException) response).printStackTrace();

    Vector tracksDetails = (Vector) response;
    List tracks = new ArrayList();
    for (Iterator itr = tracksDetails.iterator(); itr.hasNext();) {
      Hashtable trackDetails = (Hashtable) itr.next();
      String url = (String) trackDetails.get("url");
      String trackId = (String) trackDetails.get("trackId");
      String artist = (String) trackDetails.get("artist");
      String title = (String) trackDetails.get("title");
      String webSite = (String) trackDetails.get("www");
      Track track = new Track(new URL(url));
      track.setId(trackId);
      track.setArtist(artist);
      track.setTitle(title);
      try {
        if (webSite != null && webSite.length() != 0)
          track.setWebSite(new URL(webSite));
      } catch (MalformedURLException e) {
        e.printStackTrace();
      }
      tracks.add(track);
    }

    return (Track[]) tracks.toArray(new Track[tracks.size()]);
  }

  private void updateTrackDatabase(TrackDatabase trackDatabase, Track[] tracks) {
    int updated = 0;
    int added = 0;
    for (int i = 0; i < tracks.length; i++) {
      Track track = tracks[i];
      Track originalTrack = trackDatabase.getTrack(track.getKey());
      if (originalTrack == null) {
        trackDatabase.add(track);
        added++;
      } else {
        originalTrack.setId(track.getId());
        updated++;
      }
    }
    System.out.print("Added " + added + " Updated " + updated);
  }
}
