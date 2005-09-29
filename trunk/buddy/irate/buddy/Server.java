/*
 * Created on 27/09/2005
 */
package irate.buddy;

import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sleepycat.je.DatabaseException;

public class Server {

  public static void main(String args[]) {
       
    String account = args[0];
    String password = args[1];

    try {
      Logger.global.setLevel(Level.FINEST);
      
      Buddy buddy = new Buddy(true);
      String userId = buddy.login(account, password, true);
      System.out.println("Login: " + userId);
      buddy.logout(userId);

      // WebServer webServer = new WebServer(8031);
      // webServer.addHandler("Buddy", buddy);
      // webServer.start();
      buddy.close();
    }
    catch (DatabaseException e) {
      e.printStackTrace();
    }
  }
}
