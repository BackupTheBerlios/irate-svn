package irate.server;

import java.io.*;
import java.util.*;

public class UserList {

  private File userDir;
  private Vector users = new Vector();
  
  public UserList() {
    userDir = new File("users");
    if (!userDir.exists())
      userDir.mkdir();
    File[] files = userDir.listFiles();
    for (int i = 0; i < files.length; i++)
      if (files[i].getName().toLowerCase().endsWith(".xml")) 
        try {
          users.add(new ServerDatabase(files[i]));
        }
        catch (IOException e) {
          e.printStackTrace();
        }
  }

  public ServerDatabase getUser(String name) {
    for (int i = 0; i < users.size(); i++) {
      ServerDatabase user = (ServerDatabase) users.elementAt(i);
      if (name.equals(user.getUserName()))
        return user;
    }
    return null;
  }

  public ServerDatabase[] getUsers() {
    return (ServerDatabase[]) users.toArray(new ServerDatabase[users.size()]);
  }
}
