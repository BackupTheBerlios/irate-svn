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
 * Date Updated: $$Date: 2003/09/21 11:59:03 $$
 * @author Creator:	Robin <robin@kallisti.net.nz> (eythain)
 * @author Updated:	$$Author: eythian $$
 * @version $$Revision: 1.5 $$
 */
public class ExternalControlConfigurator {
  private Display display;
  private PluginApplication app;
  private PluginManager pluginManager;
  private ExternalControlPlugin plugin;
  private boolean done = false;
  private Text port;
  private Text simConn;
  private Button localhostOnly;
  private Button requirePassword;
  private Text password;

  public ExternalControlConfigurator(Display display_, PluginApplication app_, Plugin plugin_)
  {
    this.display = display_;
    this.app = app_;
    this.plugin = (ExternalControlPlugin) plugin_;

    final Shell shell = new Shell(display);
    shell.setText("External control configuration");
    shell.addShellListener(new ShellAdapter() {
      public void shellClosed(ShellEvent e){
        done=true;
      }
    });
    GridLayout layout = new GridLayout(2, false);
    shell.setLayout(layout);

    ModifyListener callSetup = new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        setup();
      }
    };

    SelectionListener buttonSelected = new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {
        setup();
      }

      public void widgetSelected(SelectionEvent e) {
        setup();
      }
    };
      
    new Label(shell, SWT.NONE).setText("Port");
    port = new Text(shell, SWT.SINGLE | SWT.BORDER);
    port.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
    port.setText(Integer.toString(plugin.getPort()));
    port.addModifyListener(callSetup);

    new Label(shell, SWT.NONE).setText("Simultanious connections");
    simConn = new Text(shell, SWT.SINGLE | SWT.BORDER);
    simConn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
    simConn.setText(Integer.toString(plugin.getSimConnections()));
    simConn.addModifyListener(callSetup);

    new Label(shell, SWT.NONE).setText("Localhost only");
    localhostOnly = new Button(shell, SWT.CHECK);
    localhostOnly.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
    localhostOnly.setSelection(plugin.getLocalhostOnly());
    localhostOnly.addSelectionListener(buttonSelected);

    new Label(shell, SWT.NONE).setText("Require password");
    requirePassword = new Button(shell, SWT.CHECK);
    requirePassword.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
    requirePassword.setSelection(plugin.getRequirePassword());
    requirePassword.addSelectionListener(buttonSelected);
    
    new Label(shell, SWT.NONE).setText("Password");
    password = new Text(shell, SWT.SINGLE | SWT.BORDER);
    password.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
    password.setText(plugin.getPassword());
    password.addModifyListener(callSetup);

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
    shell.dispose();
  }


  private void setup()
  {
    try {
      if (!done) {
        plugin.setPort(Integer.parseInt(port.getText()));
        plugin.setSimConnections(Integer.parseInt(simConn.getText()));
        plugin.setLocalhostOnly(localhostOnly.getSelection());
        plugin.setRequirePassword(requirePassword.getSelection());
        plugin.setPassword(password.getText());
      }
    }
    catch (NumberFormatException e) {
    }
  }
}
