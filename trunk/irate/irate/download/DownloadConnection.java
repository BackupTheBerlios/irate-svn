/* DownloadConnection.java
 *
 * $Id: DownloadConnection.java,v 1.2 2004/02/12 22:08:09 enz Exp $
 * $Source: /tmp/irate/irate/irate/download/DownloadConnection.java,v $
 */

package irate.download;

import java.io.*;
import java.net.*;

/** Wrapper class around URLConnection for downloading with timeout.
 *  This class can be used similar to URLConnection, but allows
 *  to specify a timeout value on operations.
 *  If a timeout happens once, all future function calls will
 *  immediately timeout.
 */
public class DownloadConnection {

  public class ResumeNotSupportedException extends IOException {
  }

  public class TimeoutException extends IOException {
  }

  public DownloadConnection(URL url) {
    this.url = url;
  }

  /** Close the communication link.
   *  @param timeout the timeout in milliseconds
   *  @exception TimeoutException on timeout
   *  @exception IOException passed from InputStream.close()
   */
  public void close(long timeout) throws IOException {
    if (isTimedOut)
      throw new TimeoutException();
    IOOperation operation = new IOOperation() {
        protected void runIOOperation() throws IOException {
          connection.getInputStream().close();
        };
      };
    runOrTimeout(operation, timeout);
  }
  
  /** Open a communication link.
   *  @param continueOffset offset for resuming a download, set to 0
   *  to download from the beginning
   *  @param timeout the timeout in milliseconds
   *  @exception TimeoutException on timeout
   *  @exception ResumeNotSupportedException if server does not support
   *  resuming a download
   *  @exception IOException passed from URLConnection.openConnection or
   *  URLConnection.connect
   */
  public void connect(long continueOffset, long timeout) throws IOException {
    if (isTimedOut)
      throw new TimeoutException();
    this.continueOffset = continueOffset;
    IOOperation operation = new IOOperation() {
        protected void runIOOperation() throws IOException {
          connection = url.openConnection();
          DownloadConnection downloadConnection = DownloadConnection.this;
          if (downloadConnection.continueOffset > 0) {
            String range = "bytes=" + downloadConnection.continueOffset + "-";
            connection.setRequestProperty("Range", range);
          }
          connection.connect();
        };
      };
    runOrTimeout(operation, timeout);
    if (continueOffset > 0)
      if (connection.getHeaderField("Content-Range") == null)
        throw new ResumeNotSupportedException();
  }

  /** Get the content length.
   *  @return the content length, or -1 if the length is not known.
   */
  public int getContentLength() {
    if (connection == null)
      return -1;
    return connection.getContentLength();
  }

  /** Get the content type.
   *  @return the content type, or null if the type is not known.
   */
  public String getContentType() {
    if (connection == null)
      return null;
    return connection.getContentType();
  }

  /** Read from the connection.
   *  Internally this function uses it's own buffer to avoid that
   *  the passed in buffer is modified after a timeout.
   *  The internal buffer will be allocated on the first call of the
   *  function or every time the size of the passed in buffer changes.
   *  @param buffer the buffer to read into
   *  @param timeout the timeout in milliseconds
   *  @exception TimeoutException on timeout
   *  @exception IOException passed from InputStream.read
   *  @return the number of bytes read.
   */
  public int read(byte[] buffer, long timeout) throws IOException {
    if (isTimedOut)
      throw new TimeoutException();
    if (this.buffer == null || this.buffer.length != buffer.length)
      this.buffer = new byte[buffer.length];
    IOOperation operation = new IOOperation() {
        protected void runIOOperation() throws IOException {
          DownloadConnection downloadConnection = DownloadConnection.this;
          result = connection.getInputStream().read(downloadConnection.buffer);
        };
      };
    runOrTimeout(operation, timeout);
    if (result > 0)
      System.arraycopy(this.buffer, 0, buffer, 0, result);
    return result;
  }

  private boolean isTimedOut;

  private int result;

  private long continueOffset;

  private byte[] buffer;

  private URL url;

  private URLConnection connection;

  private abstract class IOOperation implements Runnable {

    public synchronized boolean isFinished() {
      return finished;
    }
    
    public synchronized IOException getException() {
      return exception;
    }
    
    public void run() {
      try {
        runIOOperation();
        setFinished();
      }
      catch (IOException exception) {
        setException(exception);
      }
    }        
    
    protected abstract void runIOOperation() throws IOException;

    private boolean finished;
    
    private IOException exception;

    private synchronized void setFinished() {
      finished = true;
    }
    
    private synchronized void setException(IOException exception) {
      this.exception = exception;
    }
  };

  private void runOrTimeout(IOOperation operation,
                            long timeout) throws IOException {
    Thread thread = new Thread(operation);
    thread.start();
    try {      
      thread.join(timeout);
    }
    catch (InterruptedException exception) {
      // Shouldn't happen
      exception.printStackTrace();
    }
    if (! operation.isFinished()) {
      isTimedOut = true;
      IOException exception = operation.getException();
      if (exception != null)
        throw exception;
      else
        throw new TimeoutException();
    }
  }

  /** For testing.
   *  Connects to an URL and prints the received bytes to System.out.
   *  @param argv[0] the URL
   *  @param argv[1] the byte offset to start downloading from
   *  @param argv[2] the number of bytes to download
   *  @param argv[3] the timeout in milliseconds
   */
  public static void main(String argv[]) {
    try {
      if (argv.length != 4) {
        System.err.println("Usage: url offset length timeout");
        return;
      }
      URL url = new URL(argv[0]);
      long offset = Long.parseLong(argv[1]);
      int length = Integer.parseInt(argv[2]);
      long timeout = Long.parseLong(argv[3]);
      DownloadConnection ct = new DownloadConnection(url);
      ct.connect(offset, timeout);
      byte[] buffer = new byte[length];
      int n = ct.read(buffer, timeout);
      System.out.println("Bytes read: " + n);
      if (n > 0) {
        System.out.write(buffer, 0, n);
        System.out.println();
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}

// Local Variables:
// c-file-style:"gnu"
// c-basic-offset:2
// indent-tabs-mode:nil
// tab-width:4
// End:
