// Copyright 2003 Stephen Blackheath

package irate.swt;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.SWT;
import irate.common.*;
import irate.plugin.*;

/**
 * @author Stephen Blackheath
 */
public class PluginDialog
{
  private boolean done = false;

  public PluginDialog(Display display, PluginManager pluginManager)
  {
    final Shell shell = new Shell(display);
    shell.addShellListener(new ShellAdapter()
    {
      public void shellClosed(ShellEvent e){
        done=true;
      }
    });
    Button btnCancel = new Button(shell, SWT.NONE);
    btnCancel.setText("Cancel");
    btnCancel.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e){
        done = true;
      }
    });
    shell.pack();    
    shell.open();
    while (!done) {
      if (!display.readAndDispatch()) display.sleep();
    } 
    shell.close();
    shell.dispose();
  }    
}
