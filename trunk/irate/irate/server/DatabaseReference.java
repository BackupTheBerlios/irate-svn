/*
 * Created on Sep 12, 2003
 */
package irate.server;

import java.io.File;
import java.io.IOException;

/**
 * @author Anthony Jones
 */
public class DatabaseReference {
  
  private File file;
  private ServerDatabase serverDatabase;
  private String userName;
  
  public DatabaseReference(File file) throws IOException {
    this.file = file;
    ServerDatabase sd = getServerDatabase();
    userName = sd.getUserName();
    discard();
  }
  
  public String getUserName() {
    return userName;
  }
  
  public boolean refersTo(ServerDatabase serverDatabase) {
    return this.serverDatabase == serverDatabase;
  }
  
  public ServerDatabase getServerDatabase() throws IOException {
    if (serverDatabase == null)
      serverDatabase = new ServerDatabase(file);
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
  
}
