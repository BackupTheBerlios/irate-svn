// Copyright 2003 Stephen Blackheath

package irate.swt.plugin.lircremote;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.SWT;
import irate.common.*;
import irate.plugin.*;
import irate.plugin.lircremote.LircRemoteControlPlugin;
import irate.plugin.lircremote.LircRemoteControlListener;

/**
 * SWT version of the configurator for the Lirc remote control.
 *
 * @author Stephen Blackheath
 */
public class LircRemoteControlConfigurator
  implements LircRemoteControlListener
{
  private Display display;
  private PluginManager pluginManager;
  private LircRemoteControlPlugin plugin;
  private boolean done = false;
  private Text host;
  private Text port;
  private Label status;

  public LircRemoteControlConfigurator(Display display, PluginApplication app, Plugin plugin_)
  {
    this.display = display;
    this.plugin = (LircRemoteControlPlugin) plugin_;
    final Shell shell = new Shell(display);
    shell.setText("Lirc remote control configuration");
    shell.addShellListener(new ShellAdapter()
    {
      public void shellClosed(ShellEvent e){
        done=true;
      }
    });
    GridLayout layout = new GridLayout(2, false);
    shell.setLayout(layout);

    new Label(shell, SWT.NONE).setText("Host");
    host = new Text(shell, SWT.SINGLE | SWT.BORDER);
    host.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));;
    host.setText(plugin.getHost());
    ModifyListener callSetup = new ModifyListener() {
      public void modifyText(ModifyEvent e) {
	setup();
      }
    };
    host.addModifyListener(callSetup);

    new Label(shell, SWT.NONE).setText("Port");
    port = new Text(shell, SWT.SINGLE | SWT.BORDER);
    port.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));;
    port.setText(Integer.toString(plugin.getPort()));
    port.addModifyListener(callSetup);

    new Label(shell, SWT.NONE).setText("Status");
    status = new Label(shell, SWT.SINGLE | SWT.BORDER);
    setStatus();

    plugin.addLircRemoteControlListener(this);
    boolean wasAttached = plugin.isAttached();
      // Detach from the application while configuring so the remote control
      // buttons don't affect the application.
    if (wasAttached)
      plugin.detach();

    Button ok = new Button(shell, SWT.NONE);
    ok.setText("OK");
    GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
    gd.horizontalSpan = 2;
    ok.setLayoutData(gd);
    ok.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e){
        done = true;
      }
    });
    shell.pack();    
    shell.open();
    while (!done) {
      if (!display.readAndDispatch()) display.sleep();
    } 
    setup();
    shell.close();
    shell.dispose();

    if (wasAttached)
      plugin.attach(app);
    plugin.removeLircRemoteControlListener(this);
  }

  private void setup()
  {
    plugin.setHost(host.getText());
    try {
      if (!done)
	plugin.setPort(Integer.parseInt(port.getText()));
    }
    catch (NumberFormatException e) {
    }
  }

  private void setStatus()
  {
    if (!done)
      status.setText(plugin.getConnectStatus() ? "connected" : "disconnected");
  }

  public void connectStatusChanged(LircRemoteControlPlugin plugin, boolean connected)
  {
    if (!done)
      display.asyncExec(new Runnable() {
	public void run() {
	  setStatus();
	}
      });
  }

  public void buttonPressed(LircRemoteControlPlugin plugin, LircRemoteControlPlugin.Button button)
  {
  }
}
