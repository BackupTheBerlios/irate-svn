// Copyright 2003 Stephen Blackheath

// Modified for the external control plugin by Robin
// <robin@kallisti.net.nz>

package irate.swt.plugin.externalcontrol;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.SWT;
import irate.plugin.*;
import irate.plugin.externalcontrol.ExternalControlPlugin;

/**
 * SWT version of the configurator for the external control plugin
 *
 * Date Created: 18/9/2003
 * Date Updated: $$Date: 2003/09/20 11:49:08 $$
 * @author Creator:	Robin <robin@kallisti.net.nz> (eythain)
 * @author Updated:	$$Author: eythian $$
 * @version $$Revision: 1.3 $$
 */
public class ExternalControlConfigurator {
  private Display display;
  private PluginApplication app;
  private PluginManager pluginManager;
  private ExternalControlPlugin plugin;
  private boolean done = false;
  private Text port;
  private Text simConn;

  public ExternalControlConfigurator(Display display_, PluginApplication app_, Plugin plugin_)
  {
    this.display = display_;
    this.app = app_;
    this.plugin = (ExternalControlPlugin) plugin_;

//    plugin.addExternalControlListener(this);
    boolean wasAttached = plugin.isAttached();
      // Detach from the application while configuring so the remote control
      // buttons don't affect the application.
    if (wasAttached)
      plugin.detach();

    final Shell shell = new Shell(display);
    shell.setText("External control configuration");
    shell.addShellListener(new ShellAdapter() {
      public void shellClosed(ShellEvent e){
        done=true;
      }
    });
    GridLayout layout = new GridLayout(2, false);
    shell.setLayout(layout);

    /*    new Label(shell, SWT.NONE).setText("Host");
    host = new Text(shell, SWT.SINGLE | SWT.BORDER);
    host.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));;
    host.setText(plugin.getHost());
    */
    ModifyListener callSetup = new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        setup();
      }
    };
    /*
    host.addModifyListener(callSetup);
    */

    new Label(shell, SWT.NONE).setText("Port");
    port = new Text(shell, SWT.SINGLE | SWT.BORDER);
    port.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));;
    port.setText(Integer.toString(plugin.getPort()));
    port.addModifyListener(callSetup);

    new Label(shell, SWT.NONE).setText("Simultanious connections");
    simConn = new Text(shell, SWT.SINGLE | SWT.BORDER);
    simConn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));;
    simConn.setText(Integer.toString(plugin.getSimConnections()));
    simConn.addModifyListener(callSetup);

    /*    new Label(shell, SWT.NONE).setText("Status");
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
    */
    org.eclipse.swt.widgets.Button ok = new org.eclipse.swt.widgets.Button(shell, SWT.NONE);
    ok.setText("OK");
    GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
    gd.horizontalSpan = 2;
    ok.setLayoutData(gd);
    ok.addSelectionListener(new SelectionAdapter(){
        public void widgetSelected(SelectionEvent e){
          setup();
          shell.close();
        }
      });
    shell.pack();    
    shell.open();
    while (!done) {
      if (!display.readAndDispatch()) display.sleep();
    } 

    if (wasAttached)
      plugin.attach(app);
    //    plugin.removeLircRemoteControlListener(this);

    shell.dispose();
  }


  private void setup()
  {
    //    plugin.setHost(host.getText());
    try {
      if (!done) {
        plugin.setPort(Integer.parseInt(port.getText()));
        plugin.setSimConnections(Integer.parseInt(simConn.getText()));
      }
    }
    catch (NumberFormatException e) {
    }
  }

  /*  private void setStatus()
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
  }*/
}
