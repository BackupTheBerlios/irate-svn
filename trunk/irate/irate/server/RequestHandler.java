// Copyright 2003 Anthony Jones

package irate.server;

import java.io.*;
import java.net.*;
import java.util.*;

public class RequestHandler {
  
  private final int socketTimeoutMs = 5000;
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
    System.out.println(new Date() + " Accepted connection from " + ia.getHostAddress() + " " + ia.getHostName());
    InputStream is = null;
    OutputStream os = null;
    try {
      socket.setSoTimeout(socketTimeoutMs);
      
      is = socket.getInputStream();
      os = socket.getOutputStream();

      String headers[] = getHeaders(is);
      byte[] buf = new byte[getContentLength(headers)];
      boolean gzip = getHeader(headers, "Content-Encoding:").trim().equals("gzip");
      if (gzip) {
        System.out.println("GZIP compression");
        is = new java.util.zip.GZIPInputStream(is);
      }
      int pos = 0;
      while (pos < buf.length) {
        int nbytes = is.read(buf, pos, buf.length - pos);
        if (nbytes < 0)
          throw new IOException("Content truncated");
        pos += nbytes;
      }
      
//      System.out.println("Request:");
//      System.out.println(new String(buf));
      
      ServerDatabase request = new ServerDatabase(null, new ByteArrayInputStream(buf));
      ServerDatabase reply = masterDatabase.processRequest(request);

//      System.out.println("Reply:");
//      System.out.println(reply.toString());
      if(gzip)
        os = new java.util.zip.GZIPOutputStream(os);
      os.write(reply.toString().getBytes());
      os.flush();

        // Print any error
      String error = reply.getErrorCode();
      if (error.length() != 0)
        System.out.println("Error: " + error);
    }
    catch (Exception e) {
      e.printStackTrace(System.out);
    }
    finally {
      if (os != null) try { os.close(); } catch (IOException e) { e.printStackTrace(); }
      if (is != null) try { is.close(); } catch (IOException e) { e.printStackTrace(); }
      System.out.println();
    }
  }
}
