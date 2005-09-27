/*
 * Created on 27/09/2005
 */
package irate.buddy;

import org.apache.xmlrpc.WebServer;


public class Server {
   public static void main(String args[])
   {
     WebServer webServer = new WebServer(8031);
     webServer.addHandler("Buddy", new Buddy());
     webServer.start();
   }   
}
