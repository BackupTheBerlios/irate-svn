package irate.swt.plugin.unratednotifier;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.SWT;
import irate.plugin.*;
import irate.plugin.unratednotifier.*;

/**
 * Date Created: Feb 13, 2004
 * Date Updated: $Date: 2004/02/22 19:45:22 $
 * @author Creator: Mathieu Mallet
 * @author Updated: $Author: emh_mark3 $
 * @version $Revision: 1.2 $ */

public class UnratedNotifierConfigurator {
  private Display display;
  private PluginApplication app;
  private PluginManager pluginManager;
  private UnratedNotifierPlugin plugin;
  private boolean done = false;
  private int notificationMode = 0;

  public UnratedNotifierConfigurator(Display display_, PluginApplication app_, Plugin plugin_)
  {
    this.display = display_;
    this.app = app_;
    this.plugin = (UnratedNotifierPlugin) plugin_;

    final Shell shell = new Shell(display);
    shell.setText(Resources.getString("configurator.title"));
    shell.addShellListener(new ShellAdapter() {
      public void shellClosed(ShellEvent e){
        done=true;
      }
    });
    GridLayout layout = new GridLayout(1, false);
    shell.setLayout(layout);

    new Label(shell, SWT.NONE).setText(Resources.getString("configurator.firstlabel"));

    notificationMode = plugin.getNotificationMode();

    final Button[] notificationModes = { 
      new Button(shell, SWT.RADIO),
      new Button(shell, SWT.RADIO),
      new Button(shell, SWT.RADIO),
      new Button(shell, SWT.RADIO) };
      
    class DoMode implements SelectionListener {
      public void widgetSelected(SelectionEvent e) {
        for (int i=0; i<notificationModes.length; i++)
          if (notificationModes[i].getSelection())
            notificationMode = i;
      }
    
      public void widgetDefaultSelected(SelectionEvent e) { }
    }
    
    for (int i = 0; i<notificationModes.length; i++)
      notificationModes[i].addSelectionListener(new DoMode());
    
    if (notificationMode >= 0 && notificationMode < notificationModes.length)
      notificationModes[notificationMode].setSelection(true);
    
    notificationModes[0].setText(Resources.getString("configurator.modes.nowhere"));
    notificationModes[1].setText(Resources.getString("configurator.modes.beginning"));
    notificationModes[2].setText(Resources.getString("configurator.modes.end"));
    notificationModes[3].setText(Resources.getString("configurator.modes.beforeend"));
    notificationModes[0].setToolTipText(Resources.getString("configurator.modes.tooltips.nowhere"));
    notificationModes[1].setToolTipText(Resources.getString("configurator.modes.tooltips.beginning"));
    notificationModes[2].setToolTipText(Resources.getString("configurator.modes.tooltips.end"));
    notificationModes[3].setToolTipText(Resources.getString("configurator.modes.tooltips.beforeend"));
    
    org.eclipse.swt.widgets.Button ok = new org.eclipse.swt.widgets.Button(shell, SWT.NONE);
    ok.setText(Resources.getString("configurator.buttons.ok"));
    GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
    gd.horizontalSpan = 2;
    ok.setLayoutData(gd);
    ok.addSelectionListener(new SelectionAdapter(){
        public void widgetSelected(SelectionEvent e){
          plugin.setNotificationMode(notificationMode);
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
} 



