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
 * Dialog box to set up the buttons that correspond to a function.
 */
public class FunctionSetup
  implements LircRemoteControlListener
{
  private Display display;
  private boolean done = false;
  private List list;

  public FunctionSetup(Display display_, PluginApplication app, LircRemoteControlPlugin plugin, Function func)
  {
    this.display = display_;
    final Shell shell = new Shell(display);
    shell.setText("Set up "+func.getName());
    shell.addShellListener(new ShellAdapter()
    {
      public void shellClosed(ShellEvent e){
        done=true;
      }
    });
    GridLayout layout = new GridLayout(1, false);
    shell.setLayout(layout);

    Label heading1 = new Label(shell, SWT.NONE);
    heading1.setText("This is the list of buttons that will trigger the");
    Label heading2 = new Label(shell, SWT.NONE);
    heading2.setText(func.getName()+" function.");
    Label heading3 = new Label(shell, SWT.NONE);
    heading3.setText("Press button on remote control to add it to the list.");

    org.eclipse.swt.widgets.Button clear = new org.eclipse.swt.widgets.Button(shell, SWT.NONE);
    clear.setText("Clear list");

    list = new List(shell, SWT.NONE);
    for (int i = 0; i < func.buttons.size(); i++) {
      irate.plugin.lircremote.Button button = (irate.plugin.lircremote.Button) func.buttons.get(i); 
      list.add(button.getID());
    }
    GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL|GridData.VERTICAL_ALIGN_FILL);
    gd.widthHint = 200;
    gd.heightHint = 150;
    list.setLayoutData(gd);

    clear.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e){
        list.removeAll();
      }
    });

    org.eclipse.swt.widgets.Button ok = new org.eclipse.swt.widgets.Button(shell, SWT.NONE);
    ok.setText("OK");
    gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
    ok.setLayoutData(gd);
    ok.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e){
        done = true;
      }
    });
    shell.pack();    
    shell.open();
    plugin.addLircRemoteControlListener(this);
    while (!done) {
      if (!display.readAndDispatch()) display.sleep();
    }
    plugin.removeLircRemoteControlListener(this);

    String[] buttonIDs = list.getItems();
    func.buttons.clear();
    for (int i = 0; i < buttonIDs.length; i++)
      func.buttons.add(new irate.plugin.lircremote.Button(buttonIDs[i], func.getRepeatPolicy()));
    shell.close();
    shell.dispose();
  }

  public void connectStatusChanged(LircRemoteControlPlugin plugin, boolean connected)
  {
  }

  public void buttonPressed(LircRemoteControlPlugin plugin, final irate.plugin.lircremote.Button button)
  {
    if (button.getRepeatCount() == 0)
      display.asyncExec(new Runnable() {
        public void run() {
          if (list.indexOf(button.getID()) < 0)
            list.add(button.getID());
        }
      });
  }
}

