// Copyright 2003 Anthony Jones, Stephen Blackheath, Taras

package irate.common;

import helliker.id3.*;
import java.io.*;
import java.net.*;
import java.util.*;

import nanoxml.XMLElement;

public class Track implements TrackDetails {

  private static final TimeZone UTC = new SimpleTimeZone(/*SimpleTimeZone.UTC_TIME*/ 2, "UTC");
  private static final int DEFAULT_RATING = 6;
  
  private XMLElement elt;
  private File dir;
  private TrackDatabase trackDatabase;
  private int downloadAttempts = 0;
  private int percent_complete = -1;

  public Track(XMLElement elt, File dir) {
    this.elt = elt;
    this.dir = dir;
  }
  
  public Track(Track track) {
    elt = new XMLElement(new Properties(), true, false);
    elt.setName("Track");
    copy(track);
  }

  public Track(URL url) {
    elt = new XMLElement(new Properties(), true, false);
    elt.setName("Track");
    setURL(url);
    setInfoFromID3Tags(new File(url.getFile()));
  }  
  
  private void copy(Track track) {
    dir = track.dir;
    setURL(track.getURL());
    setTitle(track.getTitle());
    setArtist(track.getArtist());
    setRating(track.getRawRating());
    setWeight(track.getWeight());
    setDeleted(track.getDeleted());
//    System.out.println(elt);
//    setWebURL(track.getWebURL());
    // copy other attributes
  }
  
  private void setInfoFromID3Tags(File file) {

    try {
      MP3File mp3 = new MP3File(file);

      this.setArtist(mp3.getArtist());
      this.setTitle(mp3.getTitle());
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    catch (NoMPEGFramesException e) {
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    catch (ID3v2FormatException e) {
      e.printStackTrace();
    }
    catch (CorruptHeaderException e) {
      e.printStackTrace();
    }

  }
  

  public void setTrackDatabase(TrackDatabase trackDatabase) {
    this.trackDatabase = trackDatabase;
  }

  public synchronized void updateSerial() {
    if(trackDatabase == null)
      return;

    elt.setAttribute("serial", ""+(trackDatabase.getSerial()+1));
  }

  public int getSerial() {
    String str = elt.getStringAttribute("serial");
    if(str == null)
      return 0;
    return Integer.parseInt(str);
  }
  
  public void setDownloadDir(File dir) {
    this.dir = dir;
  }

  /*package visibility*/ void setElement(XMLElement elt) {
    this.elt = elt;
  }
  
  public String toString() {
    String ratingStr = getState();
    String rating = " (" + ratingStr + "/" + getNoOfTimesPlayed()+ ")";
    String s = getName() + rating;
    if (getFile() == null)
      return "[" + s + "]";
    return s;
  }

  public String getName() {
    String artist = getArtist();
    String title = getTitle();
    if (artist.length() == 0) { 
      if (title.length() == 0)
        return "?";
      return title;
    }
    if (title.length() == 0)
      return artist;
    return artist + " / " + title;
  }

  /**
   * Return the rating with Float.NaN meaning that it hasn't been rated.
   */
  protected synchronized float getRawRating() {
    try {
      String s = elt.getStringAttribute("rating");
      if (s != null)
        return Float.parseFloat(s);
    }
    catch (NumberFormatException e) {
    }
    return Float.NaN;
  }

  public boolean isRated() {
    return !Float.isNaN(getRawRating()) && (getRawRating() >= 0);
  }

  public synchronized float getRating() {
    return getRating(DEFAULT_RATING);
  }

  public synchronized float getRating(float defaultRating) {
    if (isRated())
      return getRawRating();
    return defaultRating;
  }

  public synchronized void erase() {
    if (!isErased()) {
      File f = getFile();
      if (f != null && f.exists()) {
        try {
          f.delete();
        } 
        catch (Exception e) {
          e.printStackTrace();
        }
        setFileState("erased");
        elt.setAttribute("file", "");
      }
    }
  }
  
  public boolean isErased() {
    return getFileState().equals("erased");
  }

  public synchronized int getNoOfTimesPlayed() {
    try {
      String s = elt.getStringAttribute("played");
      if (s == null)
        s = "";
      return Integer.parseInt(s);
    }
    catch (NumberFormatException e) {
    }
    return 0;
  }
  
  private static String format(int i, int length) {
    String s = Integer.toString(i);
    while (s.length() < length) 
      s = "0" + s;
    return s;
  }

  /** Set the last played time stamp to the current time. */
  public synchronized void updateTimeStamp() {
    try {
      Calendar c = new GregorianCalendar(UTC);
      elt.setAttribute("last",
        format(c.get(Calendar.YEAR), 4) +
        format(c.get(Calendar.MONTH) + 1, 2) +
        format(c.get(Calendar.DAY_OF_MONTH), 2) +
        format(c.get(Calendar.HOUR_OF_DAY), 2) +
        format(c.get(Calendar.MINUTE), 2) +
        format(c.get(Calendar.SECOND), 2)
      );
      updateSerial();
    } 
    catch(Exception e) {
      e.printStackTrace();
    } 
  }
  
  public void incNoOfTimesPlayed() {
    synchronized (this) {
      elt.setAttribute("played", Integer.toString(getNoOfTimesPlayed() + 1));
      updateSerial();
    }
  }

  public void unSetNoOfTimesPlayed() {
    synchronized (this) {
      elt.removeAttribute("played");
      elt.removeAttribute("last");
    }
  }

  public synchronized Date getLastPlayed() {
    String s = elt.getStringAttribute("last");
    if (s != null && s.length() == 14)
      try {
        Calendar c = new GregorianCalendar(UTC);
        c.set(Calendar.YEAR, Integer.parseInt(s.substring(0, 4)));
        c.set(Calendar.MONTH, Integer.parseInt(s.substring(4, 6)) - 1);
        c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(s.substring(6, 8)));
        c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(s.substring(8, 10)));
        c.set(Calendar.MINUTE, Integer.parseInt(s.substring(10, 12)));
        c.set(Calendar.SECOND, Integer.parseInt(s.substring(12, 14)));
        return new Date(c);
      }
      catch (NumberFormatException e) { 
      }
    return new Date(null);
  }

  public synchronized void setRating(float rating) {
    if (Float.isNaN(rating))
      unSetRating();
    else
      elt.setAttribute("rating", Float.toString(rating));
    updateSerial();
  }

  public synchronized void unSetRating() {
    elt.removeAttribute("rating");
  }

  public synchronized void setWeight(float weight) {
    if (Float.isNaN(weight))
      unSetWeight();
    else
      elt.setAttribute("weight", Float.toString(weight));
    updateSerial();
  }

  public synchronized void unSetWeight() {
    elt.removeAttribute("weight");
  }

  public synchronized float getWeight() {
    try {
      String s = elt.getStringAttribute("weight");
      if (s != null)
        return Float.parseFloat(s);
    }
    catch (Exception e) {
    }
    return Float.NaN;
  }

  public synchronized void setVolume(int volume) {
    elt.setAttribute("volume", Integer.toString(volume));
  }

  public synchronized int getVolume() {
    try {
      return Integer.parseInt(elt.getStringAttribute("volume"));
    }
    catch (Exception e) {
    }
    return 0;
  }
  
  public synchronized void unSetVolume() {
    elt.removeAttribute("volume");
  }
  
  public void setBroken() {
    setFileState("broken");
  }

  private synchronized String getFileState() {
    String s = elt.getStringAttribute("state");
    if (s == null)
      return "";
    return s;
  }

  private synchronized void setFileState(String state) {
    elt.setAttribute("state", state);
  }

  public synchronized boolean isBroken() {
    if (getFileState().equals("broken"))
      return true;

      // This part is for backwards compatibility
    String s = elt.getStringAttribute("broken");
    return s != null && (s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("true"));
  }

  /**
   * True if the file is supposed to have been downloaded, but doesn't actually exist
   * on the disk.
   */
  public boolean isMissing() {
    File file = getFile();
    return file != null && !file.exists() && !isDeleted();
  }

  public boolean isDeleted() {
      return isRated() && this.getDeleted().equals("true");
  }
  
  /**
   * True if the file has not been downloaded.
   */
  public boolean isNotDownloaded()
  {
    return getFile() == null;
  }

  /**
   * True if the file is broken or rated 0, and therefore should not appear to
   * the user.
   */
  public boolean isHidden() {
    return isBroken() || getRating() == 0 || isDeleted();
  }
  
  /** Check if this file is pending a purge.
   * @return True if the file needs to be deleted, false otherwise. 
   */ 
  public boolean isPendingPurge() {
    // Files must be rated 0 to be pending purge. */
    if (getRating() != 0)
      return false;
      
    // If this is a file:// url then we don't want to delete the file.
    URL url = getURL();
    if (url.getProtocol().equals("file"))
      return false;

    // Now we return true if the file is non-null and exists.
    File file = getFile();
    if (file == null)
      return false;
      
    return file.exists();
  }

  public synchronized void setArtist(String artist) {
    elt.setAttribute("artist", artist);
  }

  public synchronized String getArtist() {
    String artist = elt.getStringAttribute("artist");
    if (artist == null)
      return "";
    return artist;
  }
  
  public synchronized void setTitle(String title) {
    elt.setAttribute("title", title);
  }
  
  public synchronized void setDeleted(String trueOrFalse) {
      elt.setAttribute("deleted", trueOrFalse);
  }
  
  public synchronized String getDeleted() {
      String deleted = elt.getStringAttribute("deleted");
      if (deleted == null)
        return "false";
      return deleted;
    }

  public synchronized String getTitle() {
    String title = elt.getStringAttribute("title");
    if (title == null)
      return "";
    return title;
  }

  public synchronized void setURL(URL url) {
    elt.setAttribute("url", url.toString());
  }
  
  public synchronized URL getURL() {
    try {
      String urlString = elt.getStringAttribute("url");
      if (urlString == null)
        urlString = "";
      return new URL(urlString);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public synchronized String getKey() {
    String key = elt.getStringAttribute("url");
    if (key == null)
      return "";
    return key;
  }

  public synchronized File getFile() {
    String filename = elt.getStringAttribute("file");
    if (filename == null || filename.length() == 0) {
      URL url = getURL();
      if (url.getProtocol().equals("file"))
        return new File(url.getFile());
      return null;
    }
    String separator = System.getProperties().getProperty("file.separator");
    int index = filename.lastIndexOf(separator);
    if (index >= 0)
      filename = filename.substring(index + 1);
    return new File(dir, filename); 
  }

  public synchronized void setFile(File file) {
    elt.setAttribute("file", file.getPath());
  }

  public synchronized void unSetFile() {
    elt.removeAttribute("file");
  }
  
  public boolean isOnPlayList() {
    int rating = Math.round(getRating());
    if (rating == 0)
      return false;

    int mod = (getNoOfTimesPlayed() % rating);
    return mod != 0 && mod != (rating / 2);
  }

  public int getProbability() {
    int noOfTimesPlayed = getNoOfTimesPlayed();
    float rating = getRating(1);
    float prob = /* rating * */ rating / (1 + noOfTimesPlayed);
    if (prob < 0)
      return 0;

      // Make it 1/1000th of the chance of being played if the number of times 
      // played is a multiple of the rating. This means that a track will get
      // played in chunks proportional to the number of times it has been 
      // played.
    if (isOnPlayList())
      prob *= 1000;
      
    return Math.round(prob * 1000);
  }

  public String getState() {
    if (isBroken()) 
      return Resources.getString("track.rating.broken");
    File file = getFile();
    if (isNotDownloaded()) {
      if(percent_complete != -1)
        return Resources.getString("track.rating.downloading") + " " + percent_complete + "%";
      return Resources.getString("track.rating.notdownloaded");
    }
    if (isMissing())
      return Resources.getString("track.rating.missing");
    if (isRated())
      return Integer.toString((int) getRating());
    return Resources.getString("track.rating.unrated");
  }
  
  public XMLElement getElement() {
    return elt;
  }

  public int compareTo(Track track) {
    String a0 = getArtist();
    String a1 = track.getArtist();
    int comp = a0.compareToIgnoreCase(a1);
    if (comp != 0)
      return comp;
    return getTitle().compareToIgnoreCase(track.getTitle());
  }
  
  public boolean exists() {
    File file = getFile();
    return file != null && file.exists();
  }
  
  public boolean equals(Object track) {
    return track instanceof Track && getURL().equals(((Track)track).getURL());
  }

  /** Returns the web site associated with this track */
  public synchronized URL getWebSite() {
    try {
    	String www = elt.getStringAttribute("www");
      if (www == null)
        return null;
      return new URL(www);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
	
  /** Returns license */
  public synchronized URL getLicense() {
    try {
      String www = elt.getStringAttribute("license");
      if (www == null)
        return null;
      return new URL(www);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
  
  public int hashCode() {
    return getKey().hashCode();
  }

  /**
   * Gets the name of the album associated with this track
   * @return A string representing the album in the track's ID3 tag
   */
  public String getAlbum() {
    try {
      MP3File mp3 = new MP3File(this.getFile());
      return mp3.getAlbum();
    }
    catch (Exception e) {
      e.printStackTrace();
      return "";
    }
  }
  
  /**
   * Gets the playing time of a track in seconds
   */
  public long getPlayingTime() {
    try {
      MP3File mp3 = new MP3File(this.getFile());
      return mp3.getPlayingTime();
    }
    catch (Exception e) {
      e.printStackTrace();
      return 0;
    }
  }
  
  /**
   * Gets the comment associated with this track
   * @return A string representing the comment in the track's ID3 tag
   */
  public String getComment() {
    try {
      MP3File mp3 = new MP3File(this.getFile());
      return mp3.getComment();
    }
    catch (Exception e) {
      e.printStackTrace();
      return "";
    }
  }
  
 
  /**
   * Gets the copyright info for this track
   * @return A string representing the copyright info in the track's ID3 tag
   */
  public String getCopyrightInfo() {
    /* First check if there is a copyright attribute, which is faster than reading the ID3 tag. */
    String copyrightInfo;
    synchronized (this) {
      copyrightInfo = elt.getStringAttribute("copyright");
      if (copyrightInfo != null)
        return copyrightInfo;

    /* If we don't have the track yet then we just return an empty copyright string. */
    if (!exists())
      return "";
    }
    
    /* If no copyright attribute then we read the ID3 tag. */
    try {
      MP3File mp3 = new MP3File(this.getFile());
      copyrightInfo = mp3.getCopyrightInfo();
    }
    catch (Exception e) {
      e.printStackTrace();
    }	
    
    /* We've either got a vlue or not got a value so let's continue. */
    if (copyrightInfo == null)
      copyrightInfo = "";
    synchronized (this) {
      elt.setAttribute("copyright", copyrightInfo);
      return copyrightInfo;
    }
  }

  /**
   * Gets the genre for this track
   * @return A string representing the genre in the track's ID3 tag or
   *   an empty string if not avaliable.
   */
  public String getGenre() {
    try {
      MP3File mp3 = new MP3File(this.getFile());
      return mp3.getGenre();
    }
    catch (Exception e) {
      e.printStackTrace();
    }		
    return "";
  }

  /**
   * Gets a string reperesentation of the playing time of this track
   * @return A string representing the playing time in the track's ID3 tag
   */
  public String getPlayingTimeString() {
    try {
      MP3File mp3 = new MP3File(this.getFile());
      return mp3.getPlayingTimeString();
    }
    catch (Exception e) {
      e.printStackTrace();
      return "";
    }
  }
  
  /**
   * Gets the year of this track
   * @return A string representing the year in the track's ID3 tag or
   *   an empty string if not avaliable.
   */
  public String getYear() {
    try {
      MP3File mp3 = new MP3File(this.getFile());
      return mp3.getYear();
    }
    catch (Exception e) {
      e.printStackTrace();
    }		
    return "";
  }

  /**
   * Get the number of times this download has been attempted this session.
   * @return the number of times the program has attempted to download the track
   * during the current session.
   */
  public int getDownloadAttempts() {
    return downloadAttempts;
  }
  
  /**
   * Increase the number of download attempts this session by one.
   */
  public void increaseDownloadAttempts() {
      downloadAttempts++;
  }

  /**
   * @param percent
   */
  public void setPercentComplete(int percent) {
    percent_complete = percent;
  }

  /**
   * Add a generalized property to this track, which will be stored in the track database.
   * This method is useful for plugins that want to associate their own data with a track.
   * @param value is the string value to set, or null to remove the property.
   */
  public synchronized void setProperty(String name, String value)
  {
    if (value == null)
      elt.removeAttribute(name);
    else
      elt.setAttribute(name, value);
  }

  /**
   * Get a generalized property from this track.
   * This method is useful for plugins that want to associate their own data with a track.
   * @return String value or null if this property does not exist.
   */
  public synchronized String getProperty(String name)
  {
    return elt.getStringAttribute(name);
  }

}
