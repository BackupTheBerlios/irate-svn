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
  
  /** The highest allowable rating. */
  public static final int MAX_RATING = 10;
  
  private final String docElementName = "TrackDatabase";
  private final String trackElementName = "Track";
  private final String userElementName = "User";
  private final String autoDownloadElementName = "AutoDownload";
  private final String playListElementName = "PlayList";
  private final String defaultHost = "server.irateradio.org";
  private final int defaultPort = 2278;
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
      if (!(e instanceof IOException))
        throw new IOException(e.toString());
      create();
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
        copy = new FastTrack(track);
        copy.setDownloadDir(downloadDir);
        docElt.addChild(copy.getElement());
        tracks.add(copy);
        hash.put(copy.getKey(), copy);
	copy.setTrackDatabase(this);
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
      hash.remove(track.getKey());
    }
    return true;
  }

  public Track[] getTracks() {
    synchronized (this) {
      Track[] tracks = new Track[this.tracks.size()];
      this.tracks.toArray(tracks);
      return tracks;
    }
  }

  public int getNoOfTracks() {
    return tracks.size();
  }

  private XMLElement getElement(String eltName) {
    Enumeration enum = docElt.enumerateChildren();
    while(enum.hasMoreElements())
    {
      XMLElement elt = (XMLElement) enum.nextElement();
      if(elt.getName().equals(eltName))
        return elt;
      }

    return null;
    /*NodeList nodeList = docElt.getElementsByTagName(eltName);

    if (nodeList.getLength() != 0) {
      Node node = nodeList.item(0);
      if (node instanceof Element)
        return (Element) node;
    }
    return null;*/
  }

  protected String getAttribute(String name, String attName) {
    XMLElement elt = getElement(name);
    if (elt == null)
      return "";

    String att = elt.getStringAttribute(attName);
    if (att == null){
      System.out.println("Can't find attribute "+name+"."+attName+" = "+elt);
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

//JDR \/ \/ \/ \/ \/ \/
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
//JDR /\ /\ /\ /\ /\ /\

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

  public int getAutoDownload() {
    try {
      return Integer.parseInt(getAttribute(autoDownloadElementName,"setting"));
    }
    catch (NumberFormatException e) {
    }
    return 0;
  }

  public void setAutoDownload(int setting) {
    setAttribute(autoDownloadElementName, "setting", Integer.toString(setting));
  }

  public int getAutoDownloadCount() {
    try {
      return Integer.parseInt(getAttribute(autoDownloadElementName,"count"));
    }
    catch (NumberFormatException e) {
    }
    return 0;
  }

  public void setPlayListLength(int length) {
    setAttribute(playListElementName, "length", Integer.toString(length));
  }

  public int getPlayListLength() {
    try {
      return Integer.parseInt(getAttribute(playListElementName, "length"));
    }
    catch (NumberFormatException e) {
    }
    return 5;
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
    return 10;
  }
/****/

  public void setAutoDownloadCount(int count) {
    setAttribute(autoDownloadElementName, "count", Integer.toString(count));
  }

  public void incNoOfPlays() {
    synchronized (this) {
      setAutoDownloadCount(getAutoDownloadCount() + 1);
    }
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
      Enumeration enum = docElt.enumerateChildren();
      while(enum.hasMoreElements()) {
        XMLElement elt = (XMLElement)enum.nextElement();
        if(!elt.getName().equals(trackElementName)) continue;
        //System.out.println(elt.toString());
        Track track = new FastTrack(elt, downloadDir);
        tracks.add(track);
        //System.out.println("key="+track.getKey());
        hash.put(track.getKey(), track);
      }
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

  public float getProbability(Track track) {
    if (track.getFile() == null)
      return 0;
    return track.getProbability();
  }

  public Track chooseTrack(Random random)
  {
    return chooseTrack(random, null);
  }

  /**
   * Choose a track from the track database, excluding tracks in 'toOmit'.
   * Ignore toOmit if it is null.
   */
  public Track chooseTrack(Random random, Hashtable toOmit) {
    Track[] tracks = getTracks();
    while (true) {
      float[] probs = new float[tracks.length];

        // Choose a minimum probability
      float minRating = (MAX_RATING - 2) * Math.abs(random.nextFloat());

      float totalProb = 0;
      for (int i = 0; i < tracks.length; i++) {
        Track track = tracks[i];
          float rating = track.getRating();

        if (rating >= minRating && (toOmit == null || !toOmit.containsKey(track)))
          totalProb += getProbability(track);

        probs[i] = totalProb;
      }

      if (totalProb == 0) {
        if (minRating < 2)
          return null;
      }
      else {
        while (true) {
          float rand = Math.abs(random.nextFloat()) * totalProb;
          for (int i = 0; i < tracks.length; i++) {
            if (toOmit != null && toOmit.containsKey(tracks[i]))
              continue;
            if (rand <= probs[i])
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
  public Track chooseUnratedTrack(Random random, Hashtable toOmit) {
    Track[] tracks = getTracks();
    List list = new Vector();
    for (int i = 0; i < tracks.length; i++) {
      Track track = tracks[i];
      if (!track.isRated() && (toOmit == null || !toOmit.containsKey(track)) && track.getFile() != null)
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
    for (int i = 0; i < tracks.length; i++)
      if (!tracks[i].isHidden() && !tracks[i].isRated() && tracks[i].getFile() != null)
        noOfUnrated++;  
    return noOfUnrated;  
  }

  private int compare(Track track0, Track track1) {
    return track0.getName().compareToIgnoreCase(track1.getName());
  }

  public void purge() {
    for (Iterator itr = tracks.iterator(); itr.hasNext(); ) {
      Track track = (Track) itr.next();
      if (track.isHidden())
        track.erase();
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
        noOfValidTracks++;
        if (tracks[i].isRated())
          noOfRated++;  
        else 
          if (!tracks[i].isHidden())
            noOfUnrated++;
      }
    }

      // Limit the number of unrated tracks a user is allowed.
    if (noOfUnrated >= 50)
      return false;
    
    return noOfValidTracks < MAX_NO_OF_UNRATED || noOfRated >= MIN_NO_OF_RATED;
  }
}
