/*
 * Created on Sep 12, 2003
 */
package irate.server;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import nanoxml.XMLElement;

/**
 * @author Anthony Jones
 */
public class DatabaseReference {
  
  private UserList userList;
  private File file;
  private ServerDatabase serverDatabase;
  private XMLElement elt;
  
  public DatabaseReference(UserList userList, File file) throws IOException {
    this.userList = userList;
    this.file = file;
  }
  
  private void load(ServerDatabase sd) {
    elt = new XMLElement(new Properties(), true, false);
    elt.setName("Friend");
    elt.setAttribute("name", sd.getUserName());
  }

  private void load() throws IOException {
    if (elt == null) {
      ServerDatabase sd = getServerDatabase();
      load(sd);
      discard();
    }
  }
  
  public String getUserName() throws IOException {
    load();
    return getUserName(elt);
  }

  public static String getUserName(XMLElement elt) {
    return elt.getStringAttribute("name");
  }

  
  public boolean refersTo(ServerDatabase serverDatabase) {
    return this.serverDatabase == serverDatabase;
  }
  
  public ServerDatabase getServerDatabase() throws IOException {
    if (serverDatabase == null) {
      serverDatabase = new ServerDatabase(userList, file);
      load(serverDatabase);
    }
    return serverDatabase;
  }
  
  public void save() throws IOException {
    serverDatabase.save();
    discard();
  }
  
    /** Discard any changes to the database since last save. */
  public void discard() {
    serverDatabase = null;
  }
  
  public XMLElement getElement() {
    return elt;
  }
  
}
