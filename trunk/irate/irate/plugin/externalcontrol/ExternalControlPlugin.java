package irate.plugin.externalcontrol;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.util.Hashtable;

import irate.plugin.*;
import irate.common.*;
import nanoxml.*;

/**
 * Plugin to allow external control of the iRATE client through a
 * network socket.
 *
 * @author Robin Sheat <robin@kallisti.net.nz>
 */

public class ExternalControlPlugin 
  extends Plugin {

  private int port;
  private IOThread socketListener;
  private PluginApplication app;
  /**
   * Set up the defaults.
   */
  public ExternalControlPlugin() {
    port = 12473;  // Default port number (12473 = RATE - sorry :)
  } // ExternalControlPlugin()

  /**
   * Gets a short identifier for this plugin.
   */
  public String getIdentifier() {
    return "external-control";
  } // getIdentifier()
	
  /**
   * Gets a short description of this plugin.
   */
  public String getDescription() {
    return "external control of iRATE";
  } // getDescription()

  /**
   * Attaches to the running application
   */
  protected synchronized void doAttach() {
    app = getApp();
    socketListener = new IOThread();
    socketListener.start();
  } // doAttach()

  /**
   * Detaches from the running application
   */
  protected synchronized void doDetach() {
    socketListener.prepareToDie();
    socketListener = null;
  } // doDetach()

  /**
   * Parses the configuration provided to allow the plugin to be set up
   */
  public void parseConfig(XMLElement elt) {
   port = Integer.parseInt(elt.getStringAttribute("port"));
  } // parseConfig(XMLElement elt)

  /**
   * Formats the configuration of the plugin by modifying the element
   */
  public void formatConfig(XMLElement elt) {
    elt.setAttribute("port",Integer.toString(port));
  } // formatConfig(XMLElement elt)

  // --- Accessors and Modifiers ---

  /**
   * Returns the current port setting
   */
  public int getPort() {
    return port;
  }

  /**
   * Sets the port to listen on.
   */
  public void setPort(int p) {
    port = p;
  }

  /* --- IOThread class --- */

  /**
   * This handles the socket listening. The constructor can be given a
   * callback object, it will pass any connections that it gets to
   * this. Currently it specifically allows only one connection at a
   * time. This may change. Maybe.
   */
  
  public class IOThread extends Thread {

    private boolean terminating = false;
    private ServerSocket socket;

    /**
     * Empty constructor, no callback is registered.
     */
    public IOThread() {
    } 

    /**
     * Runs until told otherwise, but spends most of its time
     * waiting for connections.
     */
    public void run() {
      int myPort = port;
      try {
        socket = new ServerSocket(myPort,1, 
                                  InetAddress.getByName("127.0.0.1")); 
      } catch (IOException e) {
        e.printStackTrace();
        prepareToDie();
      }
      while (!terminating) {
        System.out.println("Number of connections: "+ExternalControlCommunicator.instanceCount);
        try {
          if (port != myPort) { // if port changes, we need a new
                                // SocketServer
            myPort = port;
            socket = new ServerSocket(myPort,1, 
                                      InetAddress.getByName("127.0.0.1"));
          }
          socket.setSoTimeout(10000); // Block 10 secs to allow
                                      // termination etc.
          Socket s = socket.accept();
          // Past this point means we have gotten a connection. Yay us!
          // If at some stage we want multiple connections, we'd probably
          // spawn off a new thread here.
          ExternalControlCommunicator comm = 
            new ExternalControlCommunicator(app, s);
          comm.start();
        } catch (InterruptedIOException e) {
        } catch (IOException e) {
        }
      }
    } // run()

    /**
     * Commands the thread to shut down.
     */
    public void prepareToDie() {
      terminating = true;
      try {
        socket.close();
      } catch (IOException e) {}
    } // prepareToDie()

  } // class IOThread

} // class ExternalControlPlugin
