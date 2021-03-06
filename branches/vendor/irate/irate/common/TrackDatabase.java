package irate.common;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.w3c.dom.ls.*;
import org.xml.sax.*;

public class TrackDatabase {
  
  private final String docElementName = "TrackDatabase";
  private final String trackElementName = "Track";
  private final String userElementName = "User";
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
    try {
      createDOM();
      doc = db.newDocument();
      docElt = doc.createElement(docElementName);
      doc.appendChild(docElt);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
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
    }
  }

  public void add(Track track) {
    if (getTrack(track) == null) {
      Track copy = new Track((Element) doc.importNode(track.getElement(), false));
      docElt.appendChild(copy.getElement());
      tracks.add(copy);
      hash.put(copy.getKey(), copy);
    }
  }

  public void remove(Track track) {
    docElt.removeChild(track.getElement());
    tracks.remove(track);
    hash.remove(track.getKey());
  }
  
  public Track[] getTracks() {
    Track[] tracks = new Track[this.tracks.size()];
    this.tracks.toArray(tracks);
    return tracks;
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
    FileWriter fw = null;
    try {
      fw = new FileWriter(file);
      fw.write(toString());
    }
    finally {
      if (fw != null) try { fw.close(); } catch (IOException e) { e.printStackTrace(); }
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
  
  public void setError(String code, String url) {
    setAttribute("Error", "code", code);
    setAttribute("Error", "url", url);
  }

  public String getErrorCode() {
    return getAttribute("Error", "code");
  }

  public URL getErrorURL() {
    try {
      return new URL(getAttribute("Error", "url"));
    }
    catch (MalformedURLException e) {
      e.printStackTrace();
    }
    return null;
  }
}
