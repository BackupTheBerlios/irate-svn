/*
 * Created on 27/09/2005
 */
package irate.buddy;

import org.apache.xmlrpc.WebServer;

import com.sleepycat.je.DatabaseException;

public class Server {
  public static void main(String args[]) {
    try {
      Buddy buddy = new Buddy(true);

      WebServer webServer = new WebServer(8031);
      webServer.addHandler("Buddy", buddy);
      webServer.start();
    } catch (DatabaseException e) {
      e.printStackTrace();
    }
  }
}
