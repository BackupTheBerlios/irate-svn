package irate.swt.plugin.unratednotifier;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.SWT;
import irate.plugin.*;
import irate.plugin.unratednotifier.*;

/**
 * Date Created: Feb 13, 2004
 * Date Updated: $Date: 2004/02/17 21:11:08 $
 * @author Creator: Mathieu Mallet
 * @author Updated: $Author: emh_mark3 $
 * @version $Revision: 1.1 $ */

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
    shell.setText("Unrated Notifier configuration");
    shell.addShellListener(new ShellAdapter() {
      public void shellClosed(ShellEvent e){
        done=true;
      }
    });
    GridLayout layout = new GridLayout(1, false);
    shell.setLayout(layout);

    new Label(shell, SWT.NONE).setText("When an unrated track is playing, play a notification sound...");

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
    
    notificationModes[0].setText("nowhere");
    notificationModes[0].setToolTipText("Select this option to turn off unrated tracks notification."); 
    notificationModes[1].setText("at beginning of track");
    notificationModes[1].setToolTipText("Select this option to play a sound when an unrated track starts playing."); 
    notificationModes[2].setText("at end of track");
    notificationModes[2].setToolTipText("Select this option to play a sound when an unrated track has finished playing."); 
    notificationModes[3].setText("30 seconds before end of track");
    notificationModes[3].setToolTipText("Select this option to play a sound when an unrated track is almost finished playing."); 
    
    org.eclipse.swt.widgets.Button ok = new org.eclipse.swt.widgets.Button(shell, SWT.NONE);
    ok.setText("OK");
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



