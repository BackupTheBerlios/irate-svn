// Copyright 2003 Stephen Blackheath

package irate.plugin.lircremote;

import irate.plugin.*;
import irate.common.Track;
import java.util.List;
import java.util.Vector;
import java.net.Socket;
import java.net.SocketException;
import java.io.*;

/**
 * Plugin for remote control based on lirc (Linux/Unix).
 *
 * @author Stephen Blackheath
 */
public class LircRemoteControlPlugin
  extends Plugin
  implements LircRemoteControlListener
{
  private String host;
  private int port;
  private Vector functions;

  public abstract class Function
  {
    public Function()
    {
    }

    public abstract String getName();

    public abstract void perform();

    public List buttons = new Vector();
  }

  public static class Button
  {
    private String id;
    private int repeatCount;
    public static final int SINGLE = 0;
    public static final int REPEATING = -1;

    public Button(String id, int repeatCount)
    {
      this.id = id;
      this.repeatCount = repeatCount;
    }
    
    /**
     * The string that identifies this button.
     */
    public String getID() {return id;}
    /**
     * Repeat count, which is zero when the button is first pressed.
     */
    public int getRepeatCount() {return repeatCount;}
    
    public String toString()
    {
      return id+"("+repeatCount+")";
    }

    public boolean equals(Object other_)
    {
      if (other_ instanceof Button) {
        Button other = (Button)other_;
        if (!id.equals(other.id))
          return false;
        if (repeatCount == REPEATING || other.repeatCount == REPEATING)
          return true;
        return repeatCount == other.repeatCount;
      }
      else
        return false;
    }
  }

  public LircRemoteControlPlugin()
  {
    host = "localhost";
    port = 8765;
    functions = new Vector();
    functions.add(new Function() {
      public String getName() {return "Rate as 'This sux'";}
      public void perform() {getApp().setRating(getApp().getSelectedTrack(), 0); getApp().skip();}
    });
    functions.add(new Function() {
      public String getName() {return "Rate as 'Yawn'";}
      public void perform() {getApp().setRating(getApp().getSelectedTrack(), 2);}
    });
    functions.add(new Function() {
      public String getName() {return "Rate as 'Not bad'";}
      public void perform() {getApp().setRating(getApp().getSelectedTrack(), 5);}
    });
    functions.add(new Function() {
      public String getName() {return "Rate as 'Cool'";}
      public void perform() {getApp().setRating(getApp().getSelectedTrack(), 7);}
    });
    functions.add(new Function() {
      public String getName() {return "Rate as 'Love it'";}
      public void perform() {getApp().setRating(getApp().getSelectedTrack(), 10);}
    });
    functions.add(new Function() {
      public String getName() {return "Pause/Resume";}
      public void perform() {getApp().setPaused(!getApp().isPaused());}
    });
    functions.add(new Function() {
      public String getName() {return "Skip";}
      public void perform() {getApp().skip();}
    });

      // Cheat just to get it going for now...
    ((Function)functions.get(0)).buttons.add(new Button("tune-down denon-tuner", Button.SINGLE));
    ((Function)functions.get(1)).buttons.add(new Button("tune-up denon-tuner", Button.SINGLE));
    ((Function)functions.get(2)).buttons.add(new Button("p.scan denon-tuner", Button.SINGLE));
    ((Function)functions.get(3)).buttons.add(new Button("chan-up denon-tuner", Button.SINGLE));
    ((Function)functions.get(4)).buttons.add(new Button("chan-down denon-tuner", Button.SINGLE));
    ((Function)functions.get(5)).buttons.add(new Button("auto-mute denon-tuner", Button.SINGLE));
    ((Function)functions.get(6)).buttons.add(new Button("rf-att-on denon-tuner", Button.SINGLE));
  }

  public List getFunctions()
  {
    return functions;
  }

  /**
   * Get a short description of this plugin.
   */
  public String getDescription()
  {
    return "lirc remote control (Linux/Unix)";
  }


// ------ Listeners ----------------------------------------------------

  private Vector listeners = new Vector();

  public void addLircRemoteControlListener(LircRemoteControlListener listener)
  {
    listeners.add(listener);
    connect();
  }

  public void removeLircRemoteControlListener(LircRemoteControlListener listener)
  {
    if (listeners.size() == 1 && listeners.contains(listener))
        disconnect();
    listeners.remove(listener);
  }

  private void notifyConnectStatusChanged()
  {
    Vector listeners = (Vector) this.listeners.clone();
    for (int i = 0; i < listeners.size(); i++) {
      LircRemoteControlListener listener = (LircRemoteControlListener) listeners.get(i);
      listener.connectStatusChanged(this, connectStatus);
    }
  }

  private void notifyButtonPressed(Button button)
  {
    Vector listeners = (Vector) this.listeners.clone();
    for (int i = 0; i < listeners.size(); i++) {
      LircRemoteControlListener listener = (LircRemoteControlListener) listeners.get(i);
      listener.buttonPressed(this, button);
    }
  }


// ------ I/O ----------------------------------------------------------

  private boolean terminating;
  private Object timer = new Object();
  private IOThread ioThread;
  private Socket s;
  private BufferedReader r;

  /**
   * Subclasses to override to do real work of attaching.
   * Application is available through getApp().
   */
  protected synchronized void doAttach()
  {
    addLircRemoteControlListener(this);
  }

  /**
   * Subclasses to override to do real work of detaching.
   * Application is available through getApp().
   */
  protected synchronized void doDetach()
  {
    removeLircRemoteControlListener(this);
  }

  /**
   * True if we are attempting to connect.
   */
  private boolean connected = false;
  private boolean isConnected() {return connected;}

  /**
   * True if we have actually established a connection.
   */
  private boolean connectStatus = false;

  /**
   * Return true if we have established a connection to the lirc daemon.
   */
  public boolean getConnectStatus() {return connectStatus;}

  private void connect()
  {
    if (!connected) {
      connected = true;
      terminating = false;
      ioThread = new IOThread();
      ioThread.start();
    }
  }

  private void disconnect()
  {
    if (connected) {
      try {
        if (ioThread != null) {
          ioThread.terminating = true;
          synchronized (timer) {
            timer.notifyAll();
          }
	  Socket s = ioThread.s;
          if (s != null)
            try {s.close();} catch (IOException e) {}
	  // Work around GCJ bug:  Closing the socket, as we do above, does not
	  // terminate the thread under GCJ.  So, we will just leave it dangling.
	  // It breaks my heart to have to work around bugs like this.  :)
	  //try {ioThread.join();} catch (InterruptedException e) {}
        }
      }
      finally {
        ioThread = null;
        connected = false;
	connectStatus = false;
	notifyConnectStatusChanged();
      }
    }
  }

  public String getHost()
  {
    return host;
  }

  public synchronized void setHost(String host)
  {
    if (!this.host.equals(host)) {
      this.host = host;
      if (isConnected()) {
	disconnect();
	connect();
      }
    }
  }

  public int getPort()
  {
    return port;
  }

  public synchronized void setPort(int port)
  {
    if (this.port != port) {
      this.port = port;
      if (isConnected()) {
	disconnect();
	connect();
      }
    }
  }

  public class IOThread
    extends Thread
  {
    IOThread()
    {
    }

    public Socket s;
    public boolean terminating = false;

    public void run()
    {
      while (!terminating) {
	long connectStart = System.currentTimeMillis();
	try {
	  s = new Socket(host, port);
	    // This makes the thread wake up occasionally, which will allow any
	    // dead threads to be cleaned up.  This can result from disconnect()
	    // being called at a bad time.
	  s.setSoTimeout(10000);
	  try {
	    BufferedReader r = new BufferedReader(new InputStreamReader(s.getInputStream()));
	    try {
	      connectStatus = true;
	      notifyConnectStatusChanged();
	      while (!terminating) {
		try {
		  String buttonText = r.readLine();
		  if (terminating || buttonText == null)
		    break;
		  int space1 = buttonText.indexOf(' ');
		  if (space1 >= 0) {
		    int space2 = buttonText.indexOf(' ', space1+1);
		    String repeatCountStr = buttonText.substring(space1+1, space2);
		    String idStr = buttonText.substring(space2+1);
		    Button button = new Button(idStr, Integer.parseInt(repeatCountStr));
		    notifyButtonPressed(button);
		  }
		}
		catch (InterruptedIOException e) {
		}
              }
	    }
	    finally {
	      if (!terminating) {
		connectStatus = false;
		notifyConnectStatusChanged();
              }
	      try {r.close();} catch (IOException e) {}
	    }
	  }
	  finally {
	    try {s.close();} catch (IOException e) {}
	    s = null;
	  }
	}
	catch (NumberFormatException e) {
	    e.printStackTrace();
	}
	catch (IOException e) {
	}
	if (terminating)
	    break;
	  // If the failed connection took less than 20 seconds, then wait the remainder
	  // of the 20 seconds.  This protects against getting stuck in a tight loop. 
	long duration = System.currentTimeMillis() - connectStart;
	if (duration < 20000L && duration >= 0L) {
	  try {
	    synchronized (timer) {
	      timer.wait(20000L - duration);
	    }
	  }
	  catch (InterruptedException e) {}
	}
      }
    }
  }

  public static void main(String[] args)
  {
    LircRemoteControlPlugin plugin = new LircRemoteControlPlugin();
    plugin.setHost("tui");
    plugin.addLircRemoteControlListener(new LircRemoteControlListener() {
      public void connectStatusChanged(LircRemoteControlPlugin plugin, boolean connected)
      {
	System.out.println(connected?"** Connected":"** Disconnected");
      }

      public void buttonPressed(LircRemoteControlPlugin plugin, LircRemoteControlPlugin.Button button)
      {
	System.out.println(button);
      }
    });
    plugin.attach(null);
  }


// ------ Translate button presses to actions --------------------------

  public void connectStatusChanged(LircRemoteControlPlugin plugin, boolean connected)
  {
    System.out.println("lirc remote control: "+(connected?"connected":"disconnected"));
  }

  public void buttonPressed(LircRemoteControlPlugin plugin, LircRemoteControlPlugin.Button button)
  {
    // System.out.println("lirc remote control: "+button);
    Function function = null;
    synchronized (this) {
      outerLoop:
      for (int i = 0; i < functions.size(); i++) {
	Function thisFunction = (Function) functions.get(i);
	for (int j = 0; j < thisFunction.buttons.size(); j++) {
	  Button thisButton = (Button) thisFunction.buttons.get(j);
	  if (thisButton.equals(button)) {
	    function = thisFunction;
	    break outerLoop;
	  }
	}
      }
    }
    if (function != null) {
      System.out.println("lirc remote control: "+function.getName());
      if (getApp() != null)
	function.perform();
    }
    /*else
      System.out.println("lirc remote control: unmapped button "+button);*/
  }
}

