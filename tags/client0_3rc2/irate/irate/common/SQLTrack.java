// Copyright 2003 Anthony Jones, Stephen Blackheath, Taras

package irate.common;

import java.io.*;
import java.util.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import nanoxml.XMLElement;

public class SQLTrack extends Track{
  
  //did any fields change?
  private boolean modified = false;
  //does this track need to be inserted?
  private boolean newTrack = false;
    
  private String username = null;
  private String password = null;
  /** This constructor is called when track is already in a database table 
  @param rows: ResultSet that the current row will be extracted from
  */
  public SQLTrack(ResultSet rows, String username, String password) {
    super(new XMLElement(new Hashtable(), false, false), null);
    this.username = username;
    this.password = password;
    
    XMLElement e = getElement();
    //nice little hack
    e.parseString("<Track/>");
    try{
      ResultSetMetaData info = rows.getMetaData();
      for(int i=1;i<=info.getColumnCount();i++){
        String name = info.getColumnName(i);
        String value = rows.getString(i);
        if(value != null){
          e.addProperty(name, value);
        }
      }
    }catch(SQLException se){
      se.printStackTrace();
    }
  }
  
  /** This is to construct from an  plain xml Track */
  public SQLTrack(XMLElement e, String username, String password) {
    super(e, null);
    this.username = username;
    this.password = password;
    
    newTrack = true;
  }

  public String getSQL() {
    if(username == null || password == null)
      return getGlobalSQL();
    else
      return getUserSQL();
  }
  
  public String getUserSQL() {
    String sql="";
    XMLElement xml = getElement();
    
    if(isNew()){
      sql = "INSERT INTO userdata(userid,trackid";
      String values = "(SELECT id FROM users WHERE name='"+SQLTrackDatabase.escape(username)+"' AND password='"+SQLTrackDatabase.escape(password)+"'),(SELECT id from tracks WHERE url='"+SQLTrackDatabase.escape(xml.getStringAttribute("url"))+"')";      
      for(Enumeration e = xml.enumeratePropertyNames();e.hasMoreElements();){
        String name = e.nextElement().toString();
        String value = xml.getStringAttribute(name);
        if(value.length() == 0 || !(name.equals("userid") || name.equals("trackid") || name.equals("weight") || name.equals("rating") || name.equals("file") || name.equals("played") || name.equals("last"))){
          continue;
        }else if(values.length() > 0){
          values += ", ";
          sql += ",";
        }
        sql += name;
        values += "'"+ SQLTrackDatabase.escape(value) +"'";
      }
      sql +=  ") VALUES("+ values + ")";
    }else{
      for(Enumeration e = xml.enumeratePropertyNames();e.hasMoreElements();){
        String name = e.nextElement().toString();
        String value = xml.getStringAttribute(name);
        
        if(value.length() == 0 || !(name.equals("userid") || name.equals("trackid") || name.equals("weight") || name.equals("rating") || name.equals("file") || name.equals("played") || name.equals("last")))
          continue;
        else if(sql.length() > 0)
          sql += ",";
        sql += name + "='"+ SQLTrackDatabase.escape(value) +"'";
      }
      if(sql.length() != 0)
        sql = "UPDATE userdata SET " + sql + " WHERE userid=(SELECT id FROM users WHERE name='"+SQLTrackDatabase.escape(username)+"' AND password='"+SQLTrackDatabase.escape(password)+"') AND trackid=(SELECT id from tracks WHERE url='"+SQLTrackDatabase.escape(xml.getStringAttribute("url"))+"')";
      else System.out.println(xml.toString());
      
    }
    return sql;
    
  }
  
  private String getGlobalSQL() {
    String sql="";
    XMLElement xml = getElement();
    
    if(isNew()){
      sql = "INSERT INTO tracks(";
      String values = "";      
      for(Enumeration e = xml.enumeratePropertyNames();e.hasMoreElements();){
        String name = e.nextElement().toString();
        String value = xml.getStringAttribute(name);
        if(!(name.equals("title") || name.equals("url") || name.equals("artist"))){
          continue;
        }else if(values.length() > 0){
          values += ", ";
          sql += ",";
        }
        sql += name;
        values += "'"+ SQLTrackDatabase.escape(value) +"'";
      }
      sql +=  ") VALUES("+ values + ")";
    }else{
      for(Enumeration e = xml.enumeratePropertyNames();e.hasMoreElements();){
        String name = e.nextElement().toString();
        String value = xml.getStringAttribute(name);
        
        if(value.length() == 0)
          continue;
        else if(sql.length() > 0)
          sql += ",";
        
        sql += name + "='"+ SQLTrackDatabase.escape(value) +"'";
      }
      sql = "UPDATE tracks SET " + sql + " WHERE id="+xml.getStringAttribute("id");
      
    }
    return sql;
  }
  
  public boolean isModified() {
    return modified || newTrack;
  }

  /** Is this an sql insert(true) of update(false) */
  public boolean isNew() {
    return newTrack;
  }

  public void erase() {
    super.erase();
    modified = true;
  }
  
  public void incNoOfTimesPlayed() {
    super.incNoOfTimesPlayed();
    modified = true;
  }

  public void unSetNoOfTimesPlayed() {
    super.unSetNoOfTimesPlayed();
    modified = true;
  }

  public void setRating(float rating) {
    super.setRating(rating);
    modified = true;    
  }

  public void unSetRating() {
    super.unSetRating();
    modified = true;
  }

  public void setWeight(float weight) {
    super.setWeight(weight);
    modified = true;
  }

  public void unSetWeight() {
    super.unSetWeight();
    modified = true;
  }

  public void setBroken() {
    super.setBroken();
    modified = true;
  }

  public void setFile(File file) {
    super.setFile(file);
    modified = true;
  }

  public void unSetFile() {
    super.unSetFile();
    modified = true;
  }
    
  public void setElement(XMLElement e) {
    super.setElement(e);
    modified = true;
  }


  public int hashCode() {
    return getURL().hashCode();
  }
  
  public String toString() {
    return getElement().toString();
  }
}
