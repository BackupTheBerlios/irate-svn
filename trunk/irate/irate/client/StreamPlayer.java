package irate.client;
import java.net.*;
import java.io.*;

/**
 * This class sets up a streaming HTTP server for Irate
 * It writes the mp3 data out to 1 client who listens and plays
 * the URL is http://localhost:14444/irate.mp3
 * @author Abram Hindle abez@abez.ca
 */

public class StreamPlayer extends AbstractPlayer implements Runnable {
  protected int PORT = 14444;
  protected boolean done = false;
  void server(int port) throws IOException {
    PORT = port;
    server();
  }
  /**
   * 1. Bind To Port
   * 2. Accept 1 Connection
   * 3. Fudge some fake HTTP stuff
   * 4. Stream Bytes From File
   * 5. Keep streaming bytes from file
   * Bugs: Depending on client buffering the client could be playing different
   * music than we are sending >:( also this is not a multithreaded server :P
   */
  void server() throws IOException {
    ServerSocket serverSocket = null;
    try {
      serverSocket = new ServerSocket(PORT);
    } catch (IOException e) {
      System.err.println("Could not listen on port: "+PORT);
      System.exit(1);
    }
    
    Socket clientSocket = null;
    try {
      clientSocket = serverSocket.accept();
    } catch (IOException e) {
      System.err.println("Accept failed");
    }
    
    DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
    BufferedReader in = new BufferedReader(
          new InputStreamReader(
          clientSocket.getInputStream()));
    String inputLine, outputLine;
    boolean lastr = false;
    notifyPosition(0,1024);
    notifyBitRate(128000);
    while ((inputLine = in.readLine()) != null) {
      System.err.println(inputLine);
      if (inputLine.length() == 0) {
        break;
      }
    }
    System.err.println("Out Of Loop?");
    byte [] input = new byte[1024];
    int k;
    out.writeBytes("HTTP/1.0 200 OK\r\n");
    out.writeBytes("Content-type: audio/x-mpeg\r\n\r\n");
    System.err.println("Write Header!");
      try {
        while(!done) {
          int total = 0;
          while (!done && -1 != (k = readFromFile(input,1024))) {
            out.write(input,0,k);
            total += k;
            notifyPosition(total-k, total);
          }
          closeFile();
        }
      } catch (IOException ioe) {
        out.close();
        in.close();
      }
    out.close();
    in.close();
    clientSocket.close();
    serverSocket.close();
  }
  void serverLoop() {
    while (!done) {
      try {
        server();
      } catch (IOException e) {
        System.err.println(e);
      }
    }
  }
  /**
   * for the thread..
   */
  public void run() {
    serverLoop();
  }
  
  private DataInputStream inf = null;
  private File file = null;

  File getFile() {
    return file;
  }
  /**
   * reads a block from the currently selected file
   */
  int readFromFile(byte [] block,int length) throws java.io.IOException {
    int k;
    if (inf == null) {
      File file = getFile();
      if (file == null) { return -1; }
      inf = new DataInputStream(new FileInputStream(file));
        notifyBitRate(128000);
    }
    if (-1 == (k = inf.read(block,0,length))) {
      closeFile();  
      return -1;
    }
    return k;
  }
  void closeFile() throws java.io.IOException {
    if (inf!=null) {
      inf.close();
    }
    inf = null;
    isRunning = false;
  }
  /**
   * close the FILE not the player :P
   */
  public void close() {
    System.err.println("Close");
    try {
      closeFile();
    } catch (IOException e) {
      e.printStackTrace();
    }
    
  }
  public String getName() {
    return "http stream";
  }
  boolean paused = false;
  public boolean isPaused() {
    //System.err.println("IsPaused "+paused);
    return paused;
  }
  public void setPaused(boolean paused) { 
    //System.err.println("Set Paused: "+paused);
    this.paused = paused;
  }
  public void setVolume(int volume) {
    //System.err.println("Set Volume: "+volume);
  }
  boolean isRunning = false;
  Thread thread = null;
  /**
   * Ugh this play has to block. We have it run another thread which we control
   * through some parameters such as the file 
   */
  public void play(java.io.File file) {
    //System.err.println("Play: "+file);
    setFile(file);
              notifyPosition(0,1024);
      notifyBitRate(128000);
    isRunning = true;
    if (thread==null) {
      thread = new Thread(this);
      thread.start();
    }
    blockPlay();
  }
  /**
   * simply block til isRunning is false.
   * Should use a semaphore :P
   */
  void blockPlay() {
    //semaphore!!! 
    while (isRunning) {
      try {
        Thread.currentThread().sleep(300);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
  /**
   * set which file to play
   */
  void setFile(File file) {
    
    try {
      closeFile();
    } catch (IOException e) {
      System.err.println(e);
    }
    this.file = file;  
  }
}
