// Copyright 2003 Anthony Jones

package irate.server;

import java.io.*;
import java.net.*;

public class Server {

  public static void main(String args[]) {
    try {
      Server server = new Server();
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

  public Server() throws IOException {
    userList = new UserList();

    File file = new File("masterdatabase.xml");
    masterDatabase = new MasterDatabase(file, userList);

    requestHandler = new RequestHandler(masterDatabase);

    serverSocket = new ServerSocket(2278);
  }

  public void run() throws IOException {
    System.out.println("iRATE server started - " + masterDatabase.getNoOfTracks() + " tracks");
    while (true) {
      Socket socket = serverSocket.accept();
      requestHandler.process(socket);
    }
  }
}
