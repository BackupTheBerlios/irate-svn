package irate.server;

import irate.common.*;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.w3c.dom.ls.*;
import org.xml.sax.*;

public class RequestHandler {
  
  private MasterDatabase masterDatabase;
  
  public RequestHandler(MasterDatabase masterDatabase) {
    this.masterDatabase = masterDatabase;
  }

  private static String[] getHeaders(InputStream is) throws IOException {
    Vector headers = new Vector();
    String s = "";
    while (true) {
      int b = is.read();

      if (b < 0) {
        headers.add(s);
        break;
      }

      if (b == '\n') {
        if (s.length() == 0)
          break;
        headers.add(s);
        s = "";
      } 
      else {
        if (b != '\r') 
          s = s + (char) b;
      }
    }
    return (String[]) headers.toArray(new String[headers.size()]);
  }

  private static String getHeader(String[] headers, String header) {
    for (int i = 0; i < headers.length; i++)
      if (headers[i].startsWith(header))
        return headers[i].substring(header.length());
    return "";
  }

  private static int getContentLength(String[] headers) {
    try {
      return Integer.parseInt(getHeader(headers, "Content-Length:").trim());
    }
    catch (Exception e) {
    }
    return 0;
  }
  
  public void process(Socket socket) {
    InetAddress ia = socket.getInetAddress();
    System.out.println("Accepted connection from " + ia.getHostAddress() + " " + ia.getHostName());
    InputStream is = null;
    OutputStream os = null;
    try {
      is = socket.getInputStream();
      os = socket.getOutputStream();

      String headers[] = getHeaders(is);
      byte[] buf = new byte[getContentLength(headers)];
      int pos = 0;
      while (pos < buf.length) {
        int nbytes = is.read(buf, pos, buf.length - pos);
        if (nbytes < 0)
          throw new IOException("Content truncated");
        pos += nbytes;
      }
      
//      System.out.println("Request:");
//      System.out.println(new String(buf));
      
      ServerDatabase request = new ServerDatabase(new ByteArrayInputStream(buf));
      ServerDatabase reply = masterDatabase.processRequest(request);

//      System.out.println("Reply:");
//      System.out.println(reply.toString());
      os.write(reply.toString().getBytes());
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    finally {
      if (is != null) try { is.close(); } catch (IOException e) { e.printStackTrace(); }
      if (os != null) try { os.close(); } catch (IOException e) { e.printStackTrace(); }
      System.out.println();
    }
  }
}
