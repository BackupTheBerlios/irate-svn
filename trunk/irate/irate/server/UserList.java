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
    System.out.print("Loading users");
    for (int i = 0; i < files.length; i++)
      if (files[i].getName().toLowerCase().endsWith(".xml")) 
        try {
          DatabaseReference ref = new DatabaseReference(files[i]);
          users.add(ref);
          if ((i % 100) == 0)
            System.out.print(".");
        }
        catch (IOException e) {
          System.out.println();
          e.printStackTrace();
        }
    System.out.println();
    System.out.println(users.size() + " users");
  }

  public DatabaseReference getUser(String name) {
    for (int i = 0; i < users.size(); i++) {
      DatabaseReference user = (DatabaseReference) users.elementAt(i);
      if (name.equals(user.getUserName()))
        return user;
    }
    return null;
  }

  public DatabaseReference createUser(String name, String password) throws IOException {
    File file = new File(userDir, name + ".xml");
    ServerDatabase user = new ServerDatabase();
    user.setFile(file);
    user.setUserName(name);
    user.setPassword(password);
    user.save();    
    users.add(user);
    return new DatabaseReference(file);
  }
  
  public DatabaseReference[] getUsers() {
    return (DatabaseReference[]) users.toArray(new DatabaseReference[users.size()]);
  }
  
  public DatabaseReference randomUser(Random random, ServerDatabase user) {
    if (users.size() == 0) 
      return null; 
    if (users.size() == 1 && users.elementAt(0) == user)
      return null;
    
    while (true) {
      DatabaseReference peer = (DatabaseReference) users.elementAt((random.nextInt() & 0x7fffffff) % users.size());
      if (!peer.refersTo(user)) 
        return peer;
    }
  }
  
  public void discardAll() {
    DatabaseReference[] users = getUsers();
    for (int i = 0; i < users.length; i++)
      users[i].discard();
  }
}
