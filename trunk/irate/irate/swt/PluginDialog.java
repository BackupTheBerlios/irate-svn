// Copyright 2003 Stephen Blackheath

package irate.swt;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.SWT;
import java.io.IOException;
import irate.common.*;
import irate.plugin.*;

/**
 * @author Stephen Blackheath
 */
public class PluginDialog
{
  private PluginManager pluginManager;
  private boolean done = false;

  public PluginDialog(Display display, PluginManager pluginManager, final PluginApplication app)
  {
    this.pluginManager = pluginManager;
    final Shell shell = new Shell(display);
    shell.setText("Plug-in settings");
    shell.addShellListener(new ShellAdapter()
    {
      public void shellClosed(ShellEvent e){
        done=true;
      }
    });
    GridLayout layout = new GridLayout(2, false);
    shell.setLayout(layout);

    Label heading = new Label(shell, SWT.NONE);
    heading.setText("Select the plugins you wish to enable");
    GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
    gd.horizontalSpan = 2;
    heading.setLayoutData(gd);

    java.util.List plugins = pluginManager.getPlugins();
    for (int i = 0; i < plugins.size(); i++) {
      final Plugin plugin = (Plugin) plugins.get(i);
      final Button checkbox = new Button(shell, SWT.CHECK);
      checkbox.setText(plugin.getDescription());
      checkbox.setSelection(plugin.isAttached());
      checkbox.addSelectionListener(new SelectionAdapter(){
	public void widgetSelected(SelectionEvent e){
	  if (checkbox.getSelection())
	    plugin.attach(PluginDialog.this.pluginManager.getApp());
          else
	    plugin.detach();
	}
      });
      Button configure = new Button(shell, SWT.NONE);
      configure.setText("Configure");
      configure.addSelectionListener(new SelectionAdapter(){
	public void widgetSelected(SelectionEvent e){
	  app.getUIFactory().lookup(plugin, PluginUIFactory.CONFIGURATOR);
	}
      });
    }

    Button ok = new Button(shell, SWT.NONE);
    ok.setText("OK");
    gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
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
    try {
      pluginManager.saveConfig();
    }
    catch (IOException e) {
        // @todo Handle error better
      e.printStackTrace();
    }
    shell.close();
    shell.dispose();
  }
}
