package irate.plugin.externalcontrol;

import java.io.*;

/**
 * This interface specifies the communication between the IOThread,
 * and the object that talks to both the iRATE application, and the
 * connected program via the socket.
 *
 * @author Robin Sheat <robin@kallisti.net.nz>
 */
public interface ExternalControlCallback {

  /**
   * Makes contact with the connecting program, and talks between it
   * and the iRATE aplication.
   *
   * @param in     Input stream from the socket
   * @param out    Output stream to the socket
   */
  public void makeContact(InputStream in, OutputStream out);

}
