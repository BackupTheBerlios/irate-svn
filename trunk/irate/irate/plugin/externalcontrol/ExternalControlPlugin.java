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
  extends Plugin 
  implements ExternalControlCallback {

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
    socketListener = new IOThread(this);
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

  /**
   * Communicates with the remotely connected program using an
   * XML-based protocol, and passes that on to iRATE.
   *
   * Protocol {@link
   * http://www.kallisti.net.nz/wiki/IrateDev/ExternalControlProto
   * documentation here}.
   */
  public void makeContact(InputStream in, OutputStream out) {
    boolean notFinished = true;
    boolean fatal = false;
    try {
      while (notFinished) {
        XMLElement command = new XMLElement(new Hashtable(), true, false);
        XMLElement response = new XMLElement(new Hashtable(), true, false);
        response.setName("IrateClient");
        try {
          command.parseFromReader(new InputStreamReader(in));
        } catch (XMLParseException e) {
          fatal = true;
        }
        boolean replyNeeded = false;
        if (fatal || !command.getName().equals("Command")) {
          // Fatal error - compose response, disconnect.
          response.setAttribute("type", "error");
          response.setAttribute("errorcondition", "unknown-command");
          response.setIntAttribute("fatal",1);
          notFinished = false;
          replyNeeded = true;
        } else {
          // Check commands
          String cmdType = command.getStringAttribute("type");
          if (cmdType.equals("currenttrack")) {
            trackResponseXML(response, app.getPlayingTrack());
            response.setAttribute("source","playing");
            replyNeeded = true;

          } else if (cmdType.equals("playerstate")) {
            response.setIntAttribute("playerstate", app.isPaused()?0:1);
            replyNeeded = true;

          } else if (cmdType.equals("selected")) {
            trackResponseXML(response, app.getSelectedTrack());
            response.setAttribute("source","selected");
            replyNeeded = true;

          } else if (cmdType.equals("invert-pause")) {
            app.setPaused(!app.isPaused());
            
          } else if (cmdType.equals("pause")) {
            app.setPaused(true);
            
          } else if (cmdType.equals("unpause")) {
            app.setPaused(false);

          } else if (cmdType.equals("skip")) {
            app.skip();
            trackResponseXML(response, app.getPlayingTrack());
            response.setAttribute("source","playing");
            replyNeeded = true;          

          } else if (cmdType.equals("rateplaying")) {
            int rating = command.getIntAttribute("rate");
            app.setRating(app.getPlayingTrack(), rating);
            trackResponseXML(response, app.getSelectedTrack());
            response.setAttribute("source","playing");
            replyNeeded = true;

          } else if (cmdType.equals("rateselected")) {
            int rating = command.getIntAttribute("rate");
            app.setRating(app.getSelectedTrack(), rating);
            trackResponseXML(response, app.getSelectedTrack());
            response.setAttribute("source","selected");
            replyNeeded = true;

          } else if (cmdType.equals("disconnect")) {
            notFinished = false;

          } else { // finally, nothing matched.
            response.setAttribute("type", "error");
            response.setAttribute("errorcondition", "unknown-command");
            response.setIntAttribute("fatal",0);
            replyNeeded = true;
          }
        }
        if (replyNeeded) {
          OutputStreamWriter outSW = new OutputStreamWriter(out);
          response.write(outSW);
          outSW.write('\n');
          outSW.flush(); // If we don't flush, it doesn't get sent.
          outSW = null; // Just to avoid them hanging around unwanted.
        }
      }
    } catch (IOException e) {}
    try {
      in.close();
      out.close();
    } catch (IOException e) {}
  } // makeContact(InputStream in, OutputStream out)

  /**
   * Alters e so that it has the additional fields required to be a
   * trackinfo XML reply.
   *
   * @param e the XML element to have attributes added to it
   * @param t where the track information comes from
   */
  private void trackResponseXML(XMLElement e, Track t) {
    e.setAttribute("type","trackinfo");
    e.setAttribute("title",t.getTitle());
    e.setAttribute("artist",t.getArtist());
    e.setAttribute("url",t.getURL().toString());
    e.setAttribute("filename",t.getFile().toString());
    e.setAttribute("state",t.getState());
    e.setDoubleAttribute("rating",t.getRating());
    e.setIntAttribute("numtimes",t.getNoOfTimesPlayed());
    e.setAttribute("lastplayed",t.getLastPlayed());
  } // trackReponseXML(XMLElement e, Track t)

  /* --- IOThread class --- */

  /**
   * This handles the socket listening. The constructor can be given a
   * callback object, it will pass any connections that it gets to
   * this. Currently it specifically allows only one connection at a
   * time. This may change. Maybe.
   */
  
  public class IOThread extends Thread {

    private ExternalControlCallback callback = null;
    private boolean terminating = false;
    private ServerSocket socket;

    /**
     * Empty constructor, no callback is registered.
     */
    public IOThread() {
    } 

    /**
     * Sets the callback object
     */
    public IOThread(ExternalControlCallback c) {
      this();
      callback = c;
    } // IOThread(ExternalControlCallback)

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
          callback.makeContact(s.getInputStream(), s.getOutputStream());
          s.close();
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
