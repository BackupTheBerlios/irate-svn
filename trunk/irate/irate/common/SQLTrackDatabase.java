// Copyright 2003 Anthony Jones, Stephen Blackheath, Taras

package irate.common;

import java.io.*;
import java.util.*;
import java.sql.*;

public class SQLTrackDatabase extends TrackDatabase{
  
  private int MAX_RATING = 10;
  
  private Vector tracks = new Vector();
  private Hashtable hash = new Hashtable();
  private Connection sqlConnection;
  
  private String username = null;
  private String password = null;
    
	public SQLTrackDatabase(Connection sqlConnection, String username, String password) throws SQLException{
		this.sqlConnection = sqlConnection;
    this.username = username;
    this.password = password;
    String sql = "INVALID SQL COMMAND";

    if(username == null || password == null){
      sql = "SELECT * FROM tracks ORDER by artist,title";
    }else{
      sql = "SELECT artist, url, file, rating, last, weight, played, title FROM userdata,tracks,users WHERE users.name='"+escape(username)+"' AND users.password='"+escape(password)+"' AND users.id=userdata.userid AND tracks.id=userdata.trackid ORDER BY artist,title";
    }
    Statement query = sqlConnection.createStatement();
		ResultSet rows = query.executeQuery(sql);
    while(rows.next()){
      addSQLTrack(new SQLTrack(rows, username, password));
    }
	}
  
  public void addSQLTrack(SQLTrack track){
        tracks.add(track);
        hash.put(track.getKey(), track);
  }
  
  public Track add(Track track) {
    synchronized (this) {
      Track copy;
      if ((copy = getTrack(track)) == null) {
        copy = new SQLTrack(track.getElement(), username, password);
        addSQLTrack((SQLTrack)copy);
      }
      return copy;
    }
  }

  public boolean remove(Track track) {
    track = getTrack(track);
    if (track == null)
      return false;
    synchronized (this) {
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

  public static String escape(String value){
    int pos = 0;
    while((pos=value.indexOf('\'', pos))!=-1){
      value=value.substring(0,pos)+"\\"+value.substring(pos);
      pos+=2;
    }
    return value;
  }
  
  /** This saves all of the changes back into the db
  ideally all this should do is call commit since the db can handle
  track mods through transactions & cursors */
  public void save() throws IOException {
    String sql = "";
    try{  
      sqlConnection.setAutoCommit(false);
      Statement st = sqlConnection.createStatement();
      for(Iterator it=tracks.iterator();it.hasNext();){
        SQLTrack t = (SQLTrack)it.next();
        sql = t.getSQL();
        if(t.isModified()){
          if(sql.length() > 0){
            st.executeUpdate(sql);
          }
        }
      }
      sqlConnection.commit();
    }catch(SQLException se){
      throw new IOException(sql+"\n"+se.toString());
    }
  }

 
  public Track getTrack(String key) {
    return (Track) hash.get(key);
  }

  public Track getTrack(Track track) {
    return getTrack(track.getKey());
  }

 
  public void add(SQLTrackDatabase trackDatabase) {
    Track[] tracks = trackDatabase.getTracks();
    for (int i = 0; i < tracks.length; i++) 
      add(tracks[i]);
  }

  public boolean remove(SQLTrackDatabase trackDatabase) {
    boolean removed = false;
    Track[] tracks = trackDatabase.getTracks();
    for (int i = 0; i < tracks.length; i++) 
      removed |= remove(tracks[i]);
    return false;
  }
  
  private int compare(Track track0, Track track1) {
    return track0.getName().compareToIgnoreCase(track1.getName());
  }

  //should just do a merge operation
  public void update(TrackDatabase db) {
    Track tracks[] = db.getTracks();
    for(int i=0;i<tracks.length;i++){
      Track t = getTrack(tracks[i]);
      if(t != null && !t.getElement().equals(tracks[i].getElement()))
        t.setElement(tracks[i].getElement());
      else
        add(tracks[i]);
    }
  }
    
  public void toStream(PrintWriter out) {
    out.println("<Trackdatabase>\n");
    for(Iterator it = tracks.iterator();it.hasNext();)
      out.println( it.next().toString());
    out.println("</TrackDatabase>");
  }
}
