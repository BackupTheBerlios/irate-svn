package irate.common;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.w3c.dom.ls.*;
import org.xml.sax.*;

public class TrackDatabase {
  
  private int MAX_RATING = 10;
  
  private final String docElementName = "TrackDatabase";
  private final String trackElementName = "Track";
  private final String userElementName = "User";
  private final String autoDownloadElementName = "AutoDownload";
  private final String defaultHost = "takahe.blacksapphire.com";
  private final int defaultPort = 2278;
  private Vector tracks = new Vector();
  private Hashtable hash = new Hashtable();
  private File file;
  private DocumentBuilderFactory dbf;
  private DocumentBuilder db;
  private DOMImplementationLS dil;
  private DOMWriter dw;
  private Document doc;
  private Element docElt;
  
  public TrackDatabase() {
    create();
  }

  public TrackDatabase(File file) throws IOException {
    try {
      createDOM();
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
      createDOM();
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
      tracks = new Vector();
      hash = new Hashtable();
      createDOM();
      doc = db.newDocument();
      docElt = doc.createElement(docElementName);
      doc.appendChild(docElt);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public Track add(Track track) {
    synchronized (this) {
      Track copy;
      if ((copy = getTrack(track)) == null) {
        copy = new Track((Element) doc.importNode(track.getElement(), false));
        docElt.appendChild(copy.getElement());
        tracks.add(copy);
        hash.put(copy.getKey(), copy);
      }
      return copy;
    }
  }

  public void remove(Track track) {
    synchronized (this) {
      docElt.removeChild(track.getElement());
      tracks.remove(track);
      hash.remove(track.getKey());
    }
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

  private Element getElement(String eltName) {
    NodeList nodeList = docElt.getElementsByTagName(eltName);
    if (nodeList.getLength() != 0) {
      Node node = nodeList.item(0);
      if (node instanceof Element) 
        return (Element) node;
    }
    return null;
  }

  protected String getAttribute(String name, String attName) {
    Element elt = getElement(name);
    if (elt == null)
      return "";
    return elt.getAttribute(attName);
  }

  protected void setAttribute(String name, String attName, String attValue) {
    Element elt = getElement(name);
    if (elt == null) {
      elt = doc.createElement(name);
      docElt.appendChild(elt);
    }
    elt.setAttribute(attName, attValue);
  }
  
  public String getUserName() {
    return getAttribute(userElementName, "name");
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

  private void createDOM() throws ParserConfigurationException {
    if (dbf == null) { 
      dbf = DocumentBuilderFactory.newInstance();
      db = dbf.newDocumentBuilder();
      dil = (DOMImplementationLS) db.getDOMImplementation();
      dw = dil.createDOMWriter();
    }
  }

  public void load(File file) throws IOException, ParserConfigurationException,
      SAXException {
    this.file = file;
    load(new FileInputStream(file));
  }
      
  public void load(InputStream is) throws IOException, ParserConfigurationException,
      SAXException {
    doc = db.parse(is);
    docElt = doc.getDocumentElement();
    if (docElt.getTagName().equals(docElementName)) {
      NodeList nl = docElt.getElementsByTagName(trackElementName);
      for (int i = 0; i < nl.getLength(); i++) {
        Element elt = (Element) nl.item(i);
        Track track = new Track(elt);
        tracks.add(track);
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
    return dw.writeToString(docElt);
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
    float[] probs = new float[tracks.length]; 

      // Choose a minimum probability
    float minRating = MAX_RATING * random.nextFloat();
//    System.out.println("minRating = " + minRating);
    
    float totalProb = 0;
    for (int i = 0; i < tracks.length; i++) {
      Track track = tracks[i];
      if (track.getRating() >= minRating)
        totalProb += getProbability(track);
      probs[i] = totalProb;
    }

    if (totalProb == 0)
      return null;

    
    while (true) {
      float rand = Math.abs(random.nextFloat()) * totalProb;
//      System.out.println("r=" + Float.toString(rand));
      for (int i = 0; i < tracks.length; i++) 
        if (rand <= probs[i])
          return tracks[i];
    }
  }

  private int compare(Track track0, Track track1) {
    return track0.getName().compareTo(track1.getName());
  }

  public void purge() {
    for (int i = tracks.size() - 1; i >= 0; i--) {
      Track track = (Track) tracks.elementAt(i);
      if (track.isHidden())
        tracks.remove(i);
    }
  }

    /** A nice ol' bubble sort. This is used because it has a passable 
     * performance when the list starts off sorted. */
  public void sort() {
    synchronized (this) {
      boolean swap;
      do {
        swap = false;
        for (int i = 1; i < tracks.size(); i++) {
          Track lastTrack = (Track) tracks.elementAt(i - 1);
          Track track = (Track) tracks.elementAt(i);
          if (compare(lastTrack, track) > 0) {
            tracks.setElementAt(track, i - 1);
            tracks.setElementAt(lastTrack, i);
            swap = true;
          }
        }
      } while (swap);
    }
  }
  
}
