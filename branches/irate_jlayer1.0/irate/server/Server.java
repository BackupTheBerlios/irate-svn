// Copyright 2003 Anthony Jones

package irate.server;

import java.io.*;
import java.net.*;

public class Server {

  public static void main(String args[]) {
    int socket = 2278;
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("--listen")) {
        if (++i < args.length) 
          try {
            socket = Integer.parseInt(args[i]);
          }
          catch (NumberFormatException e) {
          }
      } 
    }
    try {
      Server server = new Server(socket);
      server.run();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  private UserList userList;
  private MasterDatabase masterDatabase;
  private RequestHandler requestHandler;
  private ServerSocket serverSocket;

  /** Create a server.
   * @param socketNo The socket number to listen to.
   */

  public Server(int socketNo) throws IOException {
    serverSocket = new ServerSocket(socketNo);
    
    userList = new UserList();

    File file = new File("masterdatabase.xml");
    System.out.println("Loading database");
    masterDatabase = new MasterDatabase(file, userList);

    requestHandler = new RequestHandler(masterDatabase);
  }

  public void run() throws IOException {
    System.out.println("iRATE server started - " + masterDatabase.getNoOfTracks() + " tracks");
    while (true) {
      Socket socket = serverSocket.accept();
      requestHandler.process(socket);
    }
  }
}
