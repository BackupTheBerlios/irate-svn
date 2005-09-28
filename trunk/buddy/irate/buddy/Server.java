/*
 * Created on 27/09/2005
 */
package irate.buddy;

import com.sleepycat.je.DatabaseException;

public class Server {

  public static void main(String args[]) {
    String account = args[0];
    String password = args[1];

    try {
      Buddy buddy = new Buddy(true);
      String result = buddy.login(account, password, true);
      System.out.println("Login: " + result);

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
