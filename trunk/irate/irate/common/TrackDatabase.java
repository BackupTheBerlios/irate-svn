// Copyright 2003 Anthony Jones, Stephen Blackheath, Taras

package irate.common;

import java.io.*;
import java.util.*;
import nanoxml.XMLElement;

public class TrackDatabase {
  
  private int MAX_RATING = 10;
  
  private final String docElementName = "TrackDatabase";
  private final String trackElementName = "Track";
  private final String userElementName = "User";
  private final String autoDownloadElementName = "AutoDownload";
  private final String defaultHost = "takahe.blacksapphire.com";
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
        copy = new FastTrack((XMLElement) track.getElement(), downloadDir);
        docElt.addChild(copy.getElement());
        tracks.add(copy);
        hash.put(copy.getKey(), copy);
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
    return getAttribute(userElementName, "name").replace('/', '.').replace('\\', '.');
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
    return host;
  }

  public void setHost(String host) {
    setAttribute(userElementName, "host", host);
  }

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
    setAttribute("PlayList", "length", Integer.toString(length));
  }
  
  public int getPlayListLength() {
    try {
      return Integer.parseInt(getAttribute("PlayList", "length"));
    }
    catch (NumberFormatException e) {
    }
    return 5;
  }

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
    load(new FileInputStream(file));
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
      FileWriter fw = null;
      try {
        fw = new FileWriter(file);
        fw.write(toString());
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
      
  public Track chooseTrack(Random random) {
    Track[] tracks = getTracks();
    while (true) {
      float[] probs = new float[tracks.length]; 

        // Choose a minimum probability
      float minRating = (MAX_RATING - 1) * random.nextFloat();
    
      float totalProb = 0;
      for (int i = 0; i < tracks.length; i++) {
        Track track = tracks[i];
        float rating = track.getRating();

        if (rating >= minRating)
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
          for (int i = 0; i < tracks.length; i++) 
            if (rand <= probs[i])
              return tracks[i];
        }
      }
    }
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
}
