package irate.plugin.externalcontrol;

import java.net.Socket;
import java.util.Hashtable;
import java.io.*;

import irate.plugin.*;
import irate.common.*;
import nanoxml.*;

/**
 *  Performs the communication between the iRATE client and the remote
 *  application. It is done in a threadable manner, so that multiple
 *  connections can be handled by different instances.
 *
 * Date Created: 20/9/2003
 * Date Updated: $$Date: 2003/09/20 10:13:25 $$
 * @author Creator:	Robin <robin@kallisti.net.nz>
 * @author Updated:	$$Author: eythian $$
 * @version $$Revision: 1.1 $$
 */

public class ExternalControlCommunicator
  extends Thread {

  private PluginApplication app = null;
  private Socket sock = null;
  static int instanceCount = 0; // Allows restrictions to be
     // placed on the number of connections allowed at a time to
     // prevent memory being filled if someone attempts a DoS.

  /**
   * Default constructor. Does nothing.
   */
  public ExternalControlCommunicator() {
  }

  /**
   * Normal constructor. Allows the iRATE application to be set, along
   * with the streams for communication.
   *
   * @param a  Instance of the iRATE application
   * @param s  The socket to talk over.
   */
  public ExternalControlCommunicator(PluginApplication a, Socket s) {
    app = a;
    sock = s;
  }

  /**
   * To be a thread, it needs a run() method.
   */
  public void run() {
    instanceCount++;
    makeContact();
    try {
      sock.close();
    } catch (IOException e) {}
    instanceCount--;
  }

  /**
   * Communicates with the remotely connected program using an
   * XML-based protocol, and passes that on to iRATE.
   *
   * Protocol {@link
   * http://www.kallisti.net.nz/wiki/IrateDev/ExternalControlProto
   * documentation here}.
   */
  public void makeContact() {
    boolean notFinished = true;
    boolean fatal = false;
    InputStream in;
    OutputStream out;
    try {
      in = sock.getInputStream();
      out = sock.getOutputStream();
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }
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
    if (t == null) {
      e.setAttribute("title","");
      e.setAttribute("artist","");
      e.setAttribute("url","");
      e.setAttribute("filename","");
      e.setAttribute("state","");
      e.setDoubleAttribute("rating",0.0);
      e.setIntAttribute("numtimes",0);
      e.setAttribute("lastplayed","");
      return;
    }
    e.setAttribute("title",t.getTitle());
    e.setAttribute("artist",t.getArtist());
    e.setAttribute("url",t.getURL().toString());
    e.setAttribute("filename",t.getFile().toString());
    e.setAttribute("state",t.getState());
    e.setDoubleAttribute("rating",t.getRating());
    e.setIntAttribute("numtimes",t.getNoOfTimesPlayed());
    e.setAttribute("lastplayed",t.getLastPlayed());
  } // trackReponseXML(XMLElement e, Track t)
  
}
