package irate.plugin.externalcontrol;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;

import irate.plugin.*;
import nanoxml.*;

/**
 * Plugin to allow external control of the iRATE client through a
 * network socket.
 *
 * Date Created: 18/9/2003
 * Date Updated: $$Date: 2004/06/15 12:13:19 $$
 * @author Creator: Robin <robin@kallisti.net.nz> (eythian)
 * @author Updated:	$$Author: blackh $$
 * @version $$Revision: 1.15 $$
 */

public class ExternalControlPlugin 
  extends Plugin {

  private int port;
  private int simConns;
  private boolean localhostOnly;
  private boolean requirePassword;
  private String password;
  private IOThread socketListener;
  private PluginApplication app;
  /**
   * Set up the defaults.
   */
  public ExternalControlPlugin() {
    port = 12473;  // Default port number (12473 = RATE - sorry :)
    simConns = 20;
    localhostOnly = true;     // Default to fairly safe...
    requirePassword = false;  // ...and also convenient.
    password = "";
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
   * Gets a long description of this plugin, for tooltips.
   */
  public String getLongDescription() {
    return "This plugin allows iRATE to be remotely controlled over a "+
      "network socket.";
  }

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
   port = elt.getIntAttribute("port",port);
   simConns = elt.getIntAttribute("simConns",simConns);
   localhostOnly = 
     elt.getBooleanAttribute("localhostOnly", "true","false",localhostOnly);
   requirePassword =
     elt.getBooleanAttribute("requirePassword","true","false",requirePassword);
   password = elt.getStringAttribute("password",password);
  } // parseConfig(XMLElement elt)

  /**
   * Formats the configuration of the plugin by modifying the element
   */
  public void formatConfig(XMLElement elt) {
    elt.setIntAttribute("port",port);
    elt.setIntAttribute("simConns",simConns);
    elt.setAttribute("localhostOnly",localhostOnly?"true":"false");
    elt.setAttribute("requirePassword",requirePassword?"true":"false");
    elt.setAttribute("password",password);
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

  /**
   * Returns the current simultaneous connection limit
   */
  public int getSimConnections() {
    return simConns;
  }

  /**
   * Sets the simultaneous connection limit
   */
  public void setSimConnections(int s) {
    simConns = s;
  }

  /**
   * Returns true if we only bind to the localhost address
   */
  public boolean getLocalhostOnly() {
    return localhostOnly;
  }

  /**
   * Specifies whether we want only localhost connections
   */
  public void setLocalhostOnly(boolean l) {
    localhostOnly = l;
  }

  /**
   * Returns true if we require password authorisation
   */
  public boolean getRequirePassword() {
    return requirePassword;
  }

  /**
   * Specifies whether a password should be required to connect
   */
  public void setRequirePassword(boolean r) {
    requirePassword = r;
  }

  /**
   * Returns the password
   */
  public String getPassword() {
    return password;
  }

  /**
   * Sets the password
   */
  public void setPassword(String p) {
    password = p;
  }

  /* --- IOThread class --- */

  /**
   * This handles the socket listening. The constructor can be given a
   * callback object, it will pass any connections that it gets to
   * this.   
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
      boolean myLocalhostOnly = localhostOnly;
      Object timer = new Object();
      try {
        if (localhostOnly) {
          socket = new ServerSocket(myPort,0, 
                                    InetAddress.getByName("127.0.0.1")); 
        } else {
          socket = new ServerSocket(myPort,0);
        }
      } catch (IOException e) {
        e.printStackTrace();
        return;
      }
      while (!terminating) {
        try {
          if ((port != myPort) || (localhostOnly != myLocalhostOnly)) { 
                                // if port or host spec changes, we need a new
                                // SocketServer
            myPort = port;
            myLocalhostOnly = localhostOnly;
            socket.close();
            if (localhostOnly) {
              socket = new ServerSocket(myPort,0, 
                                        InetAddress.getByName("127.0.0.1"));
            } else {
              socket = new ServerSocket(myPort,0);
            }
          }
          socket.setSoTimeout(10000); // Block 10 secs to allow
                                      // termination etc.
          while (ExternalControlCommunicator.instanceCount >= simConns) {
            // We may need to wait until one of the connected clients
            // disconnects
            try {
              synchronized (timer) {
                timer.wait(5000); // Pause 5 seconds
              }
            } catch (InterruptedException e) {}
          }
          Socket s = socket.accept();
          // Past this point means we have gotten a connection. Yay us!
          // Spawn a new thread of the communicator.
          ExternalControlCommunicator comm = 
            new ExternalControlCommunicator(app, s, requirePassword, password);
          comm.start();
        } catch (InterruptedIOException e) {
        } catch (IOException e) {
          e.printStackTrace();
          prepareToDie();
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
      } catch (IOException e) {
        System.err.println("ExternalControlPlugin: Error closing socket.");
      }
    } // prepareToDie()

  } // class IOThread

} // class ExternalControlPlugin
