package irate.plugin.externalcontrol;

import java.security.SecureRandom;
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
 * Date Updated: $$Date: 2004/06/10 03:18:05 $$
 * @author Creator:	Robin <robin@kallisti.net.nz> (eythain)
 * @author Updated:	$$Author: blackh $$
 * @version $$Revision: 1.5 $$
 */

public class ExternalControlCommunicator
  extends Thread {

  private PluginApplication app = null;
  private Socket sock = null;
  private boolean requirePassword;
  private String password;
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
   * with the streams for communication and authorisation information.
   *
   * @param a    Instance of the iRATE application
   * @param s    The socket to talk over.
   * @param req  True if a password is required
   * @param pw   The password
   */
  public ExternalControlCommunicator(PluginApplication a, Socket s,
                                     boolean req, String pw) {
    app = a;
    sock = s;
    requirePassword = req;
    password = pw;
  }

  /**
   * To be a thread, it needs a run() method.
   */
  public void run() {
    instanceCount++;
    makeContact();
    try {
      sock.close();
    } catch (IOException e) {
      System.err.println("ExternalControlCommunicator: Error closing socket. "+
                         "Shouldn't be a problem.");
    }
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
    if (requirePassword) {
      if (!handlePassword(in, out)) {
        try {
          in.close();
          out.close();
        } catch (IOException e) {
          System.err.println("ExternalControlCommunicator: error closing socket");
        }
        return;
      }
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
          String cmdType = command.getStringAttribute("type","");
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
            app.skip(false);
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
    } catch (IOException e) {
      // Don't really care if closing fails
    }
  } // makeContact(InputStream in, OutputStream out)

  /**
   * Handles the authorisation protocol.
   *
   * @param in  The input stream
   * @param out The output stream
   * @return    True if the login was successful
   */ 
  private boolean handlePassword(InputStream in, OutputStream out) {
    boolean finish = false;
    boolean loginSuccess = false;
    boolean sentChallenge = false;
    String challenge = "";
   try {
      while (!finish) {
        XMLElement command = new XMLElement(new Hashtable(), true, false);
        XMLElement response = new XMLElement(new Hashtable(), true, false);
        response.setName("IrateClient");
        try {
          command.parseFromReader(new InputStreamReader(in));
        } catch (XMLParseException e) {
          finish = true;
        }
      
        if (finish || !command.getName().equals("Command")) {
          // Fatal error - compose response, disconnect.
          response.setAttribute("type", "error");
          response.setAttribute("errorcondition", "unknown-command");
          response.setIntAttribute("fatal",1);
          loginSuccess = false;
          finish = true;
        } else {
          String cmdType = command.getStringAttribute("type","");
          if (cmdType.equals("login")) {
            String cmdFormat = command.getStringAttribute("format","");
            if (cmdFormat.equals("plaintext")) {
              if (command.getStringAttribute("password","").equals(password)) {
                response.setAttribute("type","login-success");
                loginSuccess = true;
                finish = true;
              } else {
                response.setAttribute("type","login-failure");
                loginSuccess = false;
                finish = true;
              }
            } else if (cmdFormat.equals("digest-md5-getchallenge")) {
              if (!sentChallenge) {
                challenge = createChallenge();
                response.setAttribute("type","login-challenge");
                response.setAttribute("challenge",challenge);
                sentChallenge = true;
              } else {
                response.setAttribute("type","login-protocol-error");
                loginSuccess = false;
                finish = true;
              }
            } else if (cmdFormat.equals("digest-md5")) {
              if (!sentChallenge) {
                response.setAttribute("type","login-protocol-error");
                loginSuccess = false;
                finish = true;
              } else {
                String expected = MD5.getHashString(password+challenge);
                String got = command.getStringAttribute("password","");
                if (expected.equals(got)) {
                  response.setAttribute("type","login-success");
                  loginSuccess = true;
                  finish = true;
                } else {
                  response.setAttribute("type","login-failure");
                  loginSuccess = false;
                  finish = true;
                }
              }
            } else {
              response.setAttribute("type","login-format-not-implemented");
            }
          } else {
            response.setAttribute("type","login-required");
          }
        }
        OutputStreamWriter outSW = new OutputStreamWriter(out);
        response.write(outSW);
        outSW.write('\n');
        outSW.flush();
        outSW = null;
      } 
    } catch (IOException e) {}
    return loginSuccess;
  }

  /**
   * Generates a string with 20 random letters (mixed case) and
   * numbers.
   *
   * @return The random string
   */
  private String createChallenge() {
    SecureRandom random = new SecureRandom();
    byte[] bytes = new byte[1];
    char[] chars = new char[20];

    for (int i=0; i<20; i++) {
      random.nextBytes(bytes);
      while ((bytes[0] > 57  || bytes[0] < 48) && // 0-9
             (bytes[0] > 90  || bytes[0] < 65) && // A-Z
             (bytes[0] > 122 || bytes[0] < 97)) { // a-z
        random.nextBytes(bytes);
      }
      chars[i] = (char)bytes[0];
    }
    return new String(chars);
  }

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
