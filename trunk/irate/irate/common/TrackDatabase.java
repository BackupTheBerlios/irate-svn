// Copyright 2003 Anthony Jones, Stephen Blackheath, Taras

package irate.common;

import java.io.*;
import java.util.*;
import nanoxml.XMLElement;

public class TrackDatabase {

  /** The maximum number of tracks a user is allowed to download before rating 
   * the minimum number of tracks. */
  public static final int MAX_NO_OF_UNRATED = 10;
  
  /** The minimum number of tacks that need to be rated. */
  public static final int MIN_NO_OF_RATED = 3;
  
  /** The maximum number of tracks on the playlist. */
  public static final int MAX_PLAY_LIST_LENGTH = 37; 
  
  /** The highest allowable rating. */
  public static final int MAX_RATING = 10;
  
  private static final String docElementName = "TrackDatabase";
  private static final String trackElementName = "Track";
  private static final String userElementName = "User";
  private static final String autoDownloadElementName = "AutoDownload";
  private static final String playListElementName = "PlayList";
  private static final String defaultHost = "server.irateradio.org";
  private static final int defaultPort = 2278;
  private Track[] tracksCachedArray;
  private TreeSet tracks;
  private Hashtable hash;
  private File file;
  private File downloadDir;
  private XMLElement docElt;

  public TrackDatabase() {
    create();
  }

  public TrackDatabase(File file) throws IOException {
    try {
      load(file);
    }
    catch (Exception e) {
      e.printStackTrace();
      create();
      if (!(e instanceof IOException))
        throw new IOException(e.toString());
        
    }
  }

  public TrackDatabase(InputStream is) throws IOException {
    try {
      load(is);
    }
    catch (Exception e) {
      e.printStackTrace();
      if (!(e instanceof IOException))
        throw new IOException(e.toString());
      create();
    }
  }

  protected XMLElement getDocElement() {
    return docElt;
  }

  private void create() {
    try {
      tracks = new TreeSet(new Comparator() {
        public int compare(Object o0, Object o1) {
          return ((Track) o0).compareTo((Track) o1);
        }
        public boolean equals(Object o0, Object o1) {
          return compare(o0, o1) == 0;
        }
      });
      hash = new Hashtable();
      docElt = new XMLElement(new Hashtable(), false, false);
      docElt.setName(docElementName);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public Track add(Track track) {
    synchronized (this) {
      Track copy;
      if ((copy = getTrack(track)) == null) {
        copy = new Track(track);
        copy.setDownloadDir(downloadDir);
        docElt.addChild(copy.getElement());
        tracks.add(copy);
        tracksCachedArray = null;
        hash.put(copy.getKey(), copy);
        /*anthony, should we be doing serial stuff to tracks done through this?*/
        //copy.setTrackDatabase(this);
      }
      return copy;
    }
  }

  public boolean remove(Track track) {
    track = getTrack(track);
    if (track == null)
      return false;
    synchronized (this) {
      docElt.removeChild(track.getElement());
      tracks.remove(track);
      tracksCachedArray = null;
      hash.remove(track.getKey());
    }
    return true;
  }

  public Track[] getTracks() {
    synchronized (this) {
      if (tracksCachedArray == null) {
        tracksCachedArray = new Track[this.tracks.size()];
        tracks.toArray(tracksCachedArray);
      }
      return tracksCachedArray;
    }
  }

  public int getNoOfTracks() {
    return tracks.size();
  }

  private XMLElement getElement(String eltName) {
    Enumeration e = docElt.enumerateChildren();
    while(e.hasMoreElements())
    {
      XMLElement elt = (XMLElement) e.nextElement();
      if(elt.getName().equals(eltName))
        return elt;
      }

    return null;
  }

  protected String getAttribute(String name, String attName) {
    XMLElement elt = getElement(name);
    if (elt == null)
      return "";

    String att = elt.getStringAttribute(attName);
    if (att == null){
      return "";
    }
    return att;
  }

  protected void setAttribute(String name, String attName, String attValue) {
    XMLElement elt = getElement(name);
    if (elt == null) {
      elt = new XMLElement(new Hashtable(), false, false);
      elt.setName(name);
      docElt.addChild(elt);
    }
    elt.setAttribute(attName, attValue);
  //  System.out.println("name="+name + " '" +attName+"="+attValue+" "+elt);
  }

  public String getUserName() {
    char[] c = getAttribute(userElementName, "name").toCharArray();
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < c.length; i++) {
      if (c[i] < ' ' || c[i] > '~' || c[i] == '/' || c[i] == '\\')
        c[i] = '.'; 
    }
    return new String(c);
  }

  public void setUserName(String name) {
    setAttribute(userElementName, "name", name);
  }

  public String getPassword() {
    return getAttribute(userElementName, "password");
  }

  public void setPassword(String password) {
    setAttribute(userElementName, "password", password);
  }

  public String getHost() {
    String host = getAttribute(userElementName, "host");
    if (host.length() == 0)
      return defaultHost;
    if (host.equals("takahe.blacksapphire.com")) {
      setHost(defaultHost);
      return defaultHost;
    }
    return host;
  }

  public void setHost(String host) {
    setAttribute(userElementName, "host", host);
  }

  public String getHTTPProxy() { //JDR
    return getAttribute(userElementName, "HTTPProxy"); //JDR
  } //JDR

  public void setHTTPProxy(String proxy) { //JDR
    setAttribute(userElementName, "HTTPProxy", proxy); //JDR
  } //JDR

  public int getHTTPProxyPort() { //JDR
    try { //JDR
      return Integer.parseInt(getAttribute(userElementName,"HTTPProxyPort")); //JDR
    } //JDR
    catch (NumberFormatException e) { //JDR
    } //JDR
    return -1;//-1 specifies the protocol default port //JDR
  } //JDR

  public void setHTTPProxyPort(int proxyPort) { //JDR
    setAttribute(userElementName, "HTTPProxyPort", Integer.toString(proxyPort)); //JDR
  } //JDR

  public int getPort() {
    try {
      return Integer.parseInt(getAttribute(userElementName,"port"));
    }
    catch (NumberFormatException e) {
    }
    return defaultPort;
  }

  public void setPort(int port) {
    setAttribute(userElementName, "port", Integer.toString(port));
  }

  /** Get the length of the playlist. See PlayListManager for more info. */
  public int getPlayListLength() {
    /* This counts the number of decent tracks, i.e. rated 5.0 or above. */ 
    Track[] tracks = getTracks();
    int playListLength = 0;
    for (int i = 0; i < tracks.length; i++) {
      Track track = tracks[i];
      if (track.isActive() && track.isRated() && track.getRating() >= 5) {
        if (++playListLength == MAX_PLAY_LIST_LENGTH)
          break;
      }
    }
    return playListLength;  
  }

/**
 * Added By Eric Dalquist - 11.09.2003
 *
 * Allows the ratio of unrated tracks per playlist generation to be stored.
 */
  public void setUnratedPlayListRatio(int length) {
    setAttribute(playListElementName, "UnratedRatio", Integer.toString(length));
  }

  public int getUnratedPlayListRatio() {
    try {
      return Integer.parseInt(getAttribute(playListElementName, "UnratedRatio"));
    }
    catch (NumberFormatException e) {
    }
    return 13;
  }

  public int getNoOfUnratedOnPlaylist() {
    int playListLength = getPlayListLength();
    int unratedPlayListRatio = getUnratedPlayListRatio();
    
    return (int)Math.round(((double)playListLength) * (((double)unratedPlayListRatio) / 100.0));
  }
  
  public boolean isRoboJockEnabled() {
    String s = getAttribute("RoboJock", "enabled").toLowerCase();
    if (s.equals("true") || s.equals("yes"))
      return true;
    return false;
  }

  public void setRoboJockEnabled(boolean enabled) {
    setAttribute("RoboJock", "enabled", enabled ? "yes" : "no");
  }

  public void setFile(File file) {
    this.file = file;
  }

  public File getDownloadDirectory() {
    return downloadDir;
  }

  public void load(File file) throws IOException {
    this.file = file;
    this.downloadDir = new File(file.getParent(), "download");
    InputStream is = new FileInputStream(file);
    load(is);
    is.close();
  }

  public void load(InputStream is) throws IOException {
    if(docElt == null)
      create();
    docElt.parseFromReader(new InputStreamReader(is));

    if (docElt.getName().equals(docElementName)) {
      Enumeration e = docElt.enumerateChildren();
      while(e.hasMoreElements()) {
        XMLElement elt = (XMLElement)e.nextElement();
        if(!elt.getName().equals(trackElementName)) continue;
        //System.out.println(elt.toString());
        Track track = new Track(elt, downloadDir);
        tracks.add(track);
        //System.out.println("key="+track.getKey());
        hash.put(track.getKey(), track);
        track.setTrackDatabase(this);
      }
      tracksCachedArray = null;
    }
  }

  public void save() throws IOException {
    synchronized (this) {
      String name = file.getPath()+".backup";
      File temporaryFile = new File(name);
      FileWriter fw = null;
      try {
        fw = new FileWriter(temporaryFile);
        fw.write("<?xml version=\"1.0\"?>\n");
        fw.write(toString());
        fw.close();
        fw = null;
//        System.out.println("Successfully Wrote: "+name );
	  // If we wrote the file successfully, then rename the temporary
	  // file to the real name of the configuration file.  This makes
	  // the writing of the new file effectively atomic.
	if (!temporaryFile.renameTo(file)) {
          System.out.println("failed at renaming " + temporaryFile + " to " + file +"\n attempting to delete"+file);
	  if(!file.delete())
            throw new IOException("Failed to delete "+ file);
	  if (!temporaryFile.renameTo(file))
	    throw new IOException("Failed to rename "+temporaryFile+" to "+file);
        }
      }
      finally {
        if (fw != null) try { fw.close(); } catch (IOException e) { e.printStackTrace(); }
      }
    }
  }

  public String toString() {
    return docElt.toString();
  }

  public String toSerialString() {
    String str = "<TrackDatabase>";
    int serial = getSerial();
    str += getElement("User")+"\n";
    Track tracks[] = getTracks();
    for(int i=0;i<tracks.length;i++){
      if(tracks[i].getSerial() >= serial)
        str+=tracks[i].getElement().toString();
    }
    str += "</TrackDatabase>";
    return str;
  }
  
  public Track getTrack(String key) {
    return (Track) hash.get(key);
  }

  public Track getTrack(Track track) {
    return getTrack(track.getKey());
  }

  public void update(TrackDatabase trackDatabase) {
    Track[] tracks = trackDatabase.getTracks();
    for (int i = 0; i < tracks.length; i++) {
      Track track = getTrack(tracks[i]);
      if (track != null) {
        remove(track);
        add(tracks[i]);
      }
    }
  }

  public void add(TrackDatabase trackDatabase) {
    Track[] tracks = trackDatabase.getTracks();
    for (int i = 0; i < tracks.length; i++)
      add(tracks[i]);
  }

  public boolean remove(TrackDatabase trackDatabase) {
    boolean removed = false;
    Track[] tracks = trackDatabase.getTracks();
    for (int i = 0; i < tracks.length; i++)
      removed |= remove(tracks[i]);
    return false;
  }

  public void setPlayer(String path) {
    setAttribute("Player", "path", path);
  }

  public String getPlayer() {
    return getAttribute("Player", "path");
  }

  public void setError(String code, String url) {
    setAttribute("Error", "code", code);
    setAttribute("Error", "url", url);
  }

  public String getErrorCode() {
    return getAttribute("Error", "code");
  }

  public String getErrorURLString() {
    return getAttribute("Error", "url");
  }

  public int getProbability(Track track) {
    if (!track.exists())
      return 0;
    return track.getProbability();
  }

  public Track chooseTrack(Random random)
  {
    return chooseTrack(random, null);
  }

  // Choose a track with a rating no larger than 'maxRating' and not in the 'omit'
  // set.  This is a slow search . . .
  public Track chooseTrack(int maxRating, Set toOmit) {
      Track[] tracks = getTracks();
      for (int i = 0; i < tracks.length; i++) {
          Track track = tracks[i];
          float rating = track.getRating();

          if (rating <= maxRating && (toOmit == null || !toOmit.contains(track)) && track.isRated()) {
              return track;
          }
      }
      return null;    
}

  /**
   * Choose a track from the track database, excluding tracks in 'toOmit'.
   * Ignore toOmit if it is null.
   */
  public Track chooseTrack(Random random, Set toOmit) {
    Track[] tracks = getTracks();
    while (true) {
      int[] probs = new int[tracks.length];

        // Choose a minimum probability
      float minRating = (MAX_RATING - 2) * Math.abs(random.nextFloat());

      int totalProb = 0;
      for (int i = 0; i < tracks.length; i++) {
        Track track = tracks[i];
        float rating = track.getRating();

        if (track.isActive() && rating >= minRating && (toOmit == null || !toOmit.contains(track)))
          totalProb += getProbability(track);

        probs[i] = totalProb;
      }

      if (totalProb == 0) {
        if (minRating < 2)
          return null;
      }
      else {
        while (true) {
          int rand = random.nextInt(totalProb);
          for (int i = 0; i < tracks.length; i++) {
            if (toOmit != null && toOmit.contains(tracks[i]))
              continue;
            if (rand < probs[i])
              return tracks[i];
          }
        }
      }
    }
  }

  /**
   * Choose an unrated track from the track database, excluding tracks in
   * 'toOmit'. Ignore toOmit if it is null.
   */
  public Track chooseUnratedTrack(Random random, Set toOmit) {
    Track[] tracks = getTracks();
    List list = new Vector();
    for (int i = 0; i < tracks.length; i++) {
      Track track = tracks[i];
      if (track.isActive() && !track.isRated() && (toOmit == null || !toOmit.contains(track)))
        list.add(track);
    }

      // If there are no unrated tracks then return null.
    if (list.size() == 0)
      return null;

    int rand = random.nextInt(list.size());

    return (Track) list.get(rand);
  }

  public int getNoOfUnrated() {
    Track[] tracks = getTracks();
    int noOfUnrated = 0;
    for (int i = 0; i < tracks.length; i++){
      Track track = tracks[i];
      if (!track.isActive() || track.isRated())
        ;
      else
        noOfUnrated++;
    }
    return noOfUnrated;  
  }

  public int getNoOfRated() {
    Track[] tracks = getTracks();
    int noOfRated = 0;
    for (int i = 0; i < tracks.length; i++) {
      Track track = tracks[i];
      if (!track.isActive() || !track.isRated())
        ;
      else
        noOfRated++;
    }
    return noOfRated;  
  }

  private int compare(Track track0, Track track1) {
    return track0.getName().compareToIgnoreCase(track1.getName());
  }

  public void purge() {
    for (Iterator itr = tracks.iterator(); itr.hasNext(); ) {
      Track track = (Track) itr.next();
      if (track.isPendingPurge())
        track.delete();
    }
  }
  
  public int getSerial() {
    String serial = docElt.getStringAttribute("serial");
    if(serial==null)
      return 0;
    return Integer.parseInt(serial);
  }

  public void incrementSerial(){
    int num = getSerial();
    num++;
    docElt.setAttribute("serial",""+num);
  }
  
  public boolean hasRatedEnoughTracks() {
    Track[] tracks = getTracks();
    int noOfRated = 0;
    int noOfUnrated = 0;
    int noOfValidTracks = 0;
    for (int i = 0; i < tracks.length; i++) {
      Track track = tracks[i];
      if (track.getFile() != null) {
        if (tracks[i].isHidden() || tracks[i].isMissing() || tracks[i].isNotDownloaded())
          ;
        else {
          noOfValidTracks++;
          if (tracks[i].isRated())
            noOfRated++;  
          else 
            noOfUnrated++;
        }
      }
    }

      // Limit the number of unrated tracks a user is allowed.
    if (noOfUnrated >= 50)
      return false;
    
    return noOfValidTracks < MAX_NO_OF_UNRATED || noOfRated >= MIN_NO_OF_RATED;
  }
  
  /** Set the directory that the files will be downloaded into.
   * @param file
   */
  public void setDownloadDir(File file) {
    downloadDir = file;
  }

}
