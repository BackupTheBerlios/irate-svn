// Copyright 2003 Stephen Blackheath

package irate.plugin.lircremote;

import irate.plugin.*;
import irate.common.Track;
import java.util.List;
import java.util.Vector;
import java.net.Socket;
import java.io.*;

/**
 * Plugin for remote control based on lirc (Linux/Unix).
 *
 * @author Stephen Blackheath
 */
public class LircRemoteControlPlugin
  extends Plugin
  implements Runnable, LircRemoteControlListener
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
      public String getName() {return "Rate as 'Pause/Resume'";}
      public void perform() {getApp().setPaused(!getApp().isPaused());}
    });
    functions.add(new Function() {
      public String getName() {return "Rate as 'Skip'";}
      public void perform() {getApp().skip();}
    });

      // Cheat just to get it going for now...
    setHost("tui");
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
   * Plugin identifier, used to map to the right configurator.
   */
  public String getIdentifier()
  {
    return "lircRemoteControl";
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
    listeners.remove(listener);
    if (listeners.size() == 0)
        disconnect();
  }

  private void notifyConnectStatusChanged(boolean connected)
  {
    Vector listeners = (Vector) this.listeners.clone();
    for (int i = 0; i < listeners.size(); i++) {
      LircRemoteControlListener listener = (LircRemoteControlListener) listeners.get(i);
      listener.connectStatusChanged(this, connected);
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

  private boolean connected = false;

  private boolean isConnected() {return connected;}

  private void connect()
  {
    if (!connected) {
      terminating = false;
      ioThread = new Thread(this);
      ioThread.start();
      connected = true;
    }
  }

  private void disconnect()
  {
    if (connected) {
      try {
        if (ioThread != null) {
          terminating = true;
          synchronized (timer) {
            timer.notifyAll();
          }
          terminating = true;
          if (r != null)
            try {r.close();} catch (IOException e) {}
          if (s != null)
            try {s.close();} catch (IOException e) {}
          ioThread.join();
        }
      }
      catch (InterruptedException e) {
      }
      finally {
        ioThread = null;
        r = null;
        s = null;
        connected = false;
      }
    }
  }

  private boolean terminating;
  private Object timer = new Object();
  private Thread ioThread;
  private Socket s;
  private BufferedReader r;

  public String getHost()
  {
    return host;
  }

  public synchronized void setHost(String host)
  {
    this.host = host;
    if (isConnected()) {
      disconnect();
      connect();
    }
  }

  public int getPort()
  {
    return port;
  }

  public synchronized void setPort(int port)
  {
    this.port = port;
    if (isConnected()) {
      disconnect();
      connect();
    }
  }

  public void run()
  {
    while (!terminating) {
      long connectStart = System.currentTimeMillis();
      try {
          s = new Socket(host, port);
          try {
              r = new BufferedReader(new InputStreamReader(s.getInputStream()));
              try {
                  notifyConnectStatusChanged(true);
                  while (true) {
                      String buttonText = r.readLine();
                      if (buttonText == null)
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
              }
              finally {
                  notifyConnectStatusChanged(false);
                  try {r.close();} catch (IOException e) {}
              }
          }
          finally {
              try {s.close();} catch (IOException e) {}
          }
      }
      catch (NumberFormatException e) {
          e.printStackTrace();
      }
      catch (IOException e) {
          System.err.println("Failed to connect to lirc remote control at "+host+":"+port+": "+e.toString());
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
      else
          System.out.println("lirc remote control: unmapped button "+button);
  }
}

