/*
 * Created on 18/07/2005
 */
package irate.download;

import irate.common.TrackDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class RemoteClassicServer implements RemoteServer {

  private int contactCount = 0;

  public void contactServer(TrackDatabase trackDatabase)
      throws DownloadException {
    try {
      setState(Resources.getString("DownloadThread.Connecting_to_server"));

      Socket socket = new Socket(trackDatabase.getHost(), trackDatabase
          .getPort());
      InputStream is = socket.getInputStream();
      setState(Resources.getString("DownloadThread.Sending_server_request"));

      OutputStream os = socket.getOutputStream();
      String str;

      if (contactCount++ > 0)
        str = trackDatabase.toSerialString();
      // send full db on first connect
      else
        str = trackDatabase.toString();

      // System.out.println("Request:");
      // System.out.println(str);
      byte[] buf = str.getBytes();
      os
          .write(("Content-Length: " + Integer.toString(buf.length) + "\r\nContent-Encoding: gzip\r\n\r\n").getBytes()); //$NON-NLS-1$ //$NON-NLS-2$
      GZIPOutputStream gos = new GZIPOutputStream(os);
      gos.write(buf);
      gos.finish();
      os.flush();
      setState(Resources.getString("DownloadThread.Receiving_server_reply"));
      TrackDatabase reply = new TrackDatabase(new GZIPInputStream(is));
      is.close();
      os.close();
      // System.out.println("reply: ");
      // System.out.println(reply.toString());
      trackDatabase.add(reply);
      trackDatabase.save();

      String errorCode = reply.getErrorCode();
      // if errorCode == "password" we can give a better prompt.
      System.out.println("DownloadThread.java:303: " + errorCode);
      if (errorCode.length() != 0) {
        throw new DownloadException(errorCode, reply.getErrorURLString());
      }
      else
        // if no error incrmement serial
        trackDatabase.incrementSerial();
    }
    catch (UnknownHostException uhe) {
      throw new DownloadException("nohost", "hostnotfound.html");
    }
    catch (ConnectException ce) {
      if (ce.getMessage().equals("Connection timed out"))
        throw new DownloadException("conntimeout", "connectiontimeout.html");
      else if (ce.getMessage().equals("Connection refused"))
        throw new DownloadException("connrefused", "connectionrefused.html");
      else
        throw new DownloadException("conntimeout", "connectionfailed.html");
    }
    catch (IOException e) {
      throw new DownloadException("serverioerror", "ioerror.html");
    }
  }

  public void setState(String state) {
    // do something
  }

}
