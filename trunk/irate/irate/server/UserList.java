// Copyright 2003 Anthony Jones

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
    System.out.println("Loading users");
    for (int i = 0; i < files.length; i++)
      if (files[i].getName().toLowerCase().endsWith(".xml")) 
        try {
          ServerDatabase user = new ServerDatabase(files[i]);
          users.add(user);
          System.out.println("  " + user.getUserName() + " " + user.getNoOfTracks());
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

  public ServerDatabase createUser(String name, String password) {
    ServerDatabase user = new ServerDatabase();
    user.setFile(new File(userDir, name + ".xml"));
    user.setUserName(name);
    user.setPassword(password);
    users.add(user);
    return user;
  }
  
  public ServerDatabase[] getUsers() {
    return (ServerDatabase[]) users.toArray(new ServerDatabase[users.size()]);
  }
  
  public ServerDatabase randomUser(Random random, ServerDatabase user) {
    if (users.size() == 0) 
      return null; 
    if (users.size() == 1 && users.elementAt(0) == user)
      return null;
    
    while (true) {
      ServerDatabase peer = (ServerDatabase) users.elementAt((random.nextInt() & 0x7fffffff) % users.size());
      if (peer != user) 
        return peer;
    }
  }
}
