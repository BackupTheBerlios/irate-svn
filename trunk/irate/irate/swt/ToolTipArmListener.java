package irate.swt;

import irate.plugin.PluginApplication;
import org.eclipse.swt.events.ArmListener;
import org.eclipse.swt.events.ArmEvent;

/** Class to show tooltips in the statusbar */
public class ToolTipArmListener
  implements ArmListener
{
  private PluginApplication app;
  private String str;

  private static Object lock = new Object();
  private static Thread thread;
  private static PluginApplication currentApp;
  private static String currentTooltip;
  private static long currentTime;
  private static long displayFor = 2000L;  /* Display tool tips on status bar for 2 seconds. */

  /**
   * @param app Gives access to the application's status bar.
   */
  public ToolTipArmListener(PluginApplication app, String str) {
    this.app = app;
    this.str = str;
  }

  public void widgetArmed(ArmEvent e)
  {
    synchronized (lock) {
      clear();
    }
    currentTooltip = str;
    currentApp = app;
    currentTime = System.currentTimeMillis();
      // 20 is higher priority than the current download state (10)
    app.addStatusMessage(20, currentTooltip);

      // Display the tooltip for one second...
    synchronized (lock) {
      if (thread == null) {
        thread = new Thread() {
          public void run()
          {
            synchronized (lock) {
              while (true) {
                long now = System.currentTimeMillis();
                long spent = now - currentTime;
                if (spent < 0 || spent >= displayFor)
                  break;
                long left = displayFor - spent;
                try {
                  lock.wait(left);
                }
                catch (InterruptedException e) {}
              }
              clear();
              if (thread == this)
                thread = null;
            }
          }
        };
        thread.start();
      }
    }
  }

  private static void clear()
  {
    if (currentTooltip != null)
      currentApp.removeStatusMessage(currentTooltip);
    currentTooltip = null;
  }
}

