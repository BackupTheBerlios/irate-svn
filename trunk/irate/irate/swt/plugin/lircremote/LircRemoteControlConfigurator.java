// Copyright 2003 Stephen Blackheath

package irate.swt.plugin.lircremote;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import irate.common.*;
import irate.plugin.*;
import irate.plugin.lircremote.LircRemoteControlPlugin;
import irate.plugin.lircremote.LircRemoteControlListener;
import irate.plugin.lircremote.Button;
import irate.plugin.lircremote.Function;

/**
 * SWT version of the configurator for the Lirc remote control.
 *
 * @author Stephen Blackheath
 */
public class LircRemoteControlConfigurator
  implements LircRemoteControlListener
{
  private Display display;
  private PluginApplication app;
  private PluginManager pluginManager;
  private LircRemoteControlPlugin plugin;
  private boolean done = false;
  private Text host;
  private Text port;
  private Label status;

  public LircRemoteControlConfigurator(Display display_, PluginApplication app_, Plugin plugin_)
  {
    this.display = display_;
    this.app = app_;
    this.plugin = (LircRemoteControlPlugin) plugin_;

    plugin.addLircRemoteControlListener(this);
    boolean wasAttached = plugin.isAttached();
      // Detach from the application while configuring so the remote control
      // buttons don't affect the application.
    if (wasAttached)
      plugin.detach();

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

    for (int i = 0; i < plugin.getFunctions().size(); i++) {
      final Function func = (Function) plugin.getFunctions().get(i);
      new Label(shell, SWT.NONE).setText(func.getName());
      org.eclipse.swt.widgets.Button setUp = new org.eclipse.swt.widgets.Button(shell, SWT.NONE);
      setUp.setText("Set up");
      setUp.addSelectionListener(new SelectionAdapter(){
	public void widgetSelected(SelectionEvent e){
          new FunctionSetup(display, app, plugin, func);
        }
      });
    }

    org.eclipse.swt.widgets.Button ok = new org.eclipse.swt.widgets.Button(shell, SWT.NONE);
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

  public void buttonPressed(LircRemoteControlPlugin plugin, irate.plugin.lircremote.Button button)
  {
  }
}
