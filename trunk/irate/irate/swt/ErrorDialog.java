// Copyright 2003 Anthony Jones

package irate.swt;

import java.io.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.*;

public class ErrorDialog {
  
  private Display display;
  private Shell shell;
  private Shell parent;

  public ErrorDialog(Display display, Shell parent) {
    this.display = display;
    this.parent = parent;
  }

  public void show(Reader r) {
    if (shell == null) {
      createShell();
      createText(r);
      createCloseButton();
      shell.setSize(500, 300);
//      shell.pack();
      Point size = shell.getSize();
      Point ploc = parent.getLocation();
      Point psize = parent.getSize();
      //shell.setLocation(ploc.x + (psize.x - size.x) / 2, ploc.y + (psize.y - size.y) / 2);

        // Open the window and process the events.
      shell.open();
    }
  }
  
  private void createShell() {
    shell = new Shell(display);
    GridLayout layout = new GridLayout(1, false);
    shell.setLayout(layout);
    shell.addShellListener(new ShellAdapter() {
      public void shellClosed(ShellEvent e){
        actionClose();
      }
    });
  }

  private void createText(Reader r) {
    try {
      StringBuffer sb = new StringBuffer();
      
      char[] buf = new char[512];
      int nbytes;
      while ((nbytes = r.read(buf, 0, buf.length)) != -1) 
        sb.append(new String(buf, 0, nbytes));

      createText(sb.toString());
    }
    catch (Exception e) {
      e.printStackTrace();
      createText(e.toString());
    }
  }

  private void createText(String s) {
//    Label text = new Label(shell,SWT.MULTI | SWT.WRAP | SWT.BORDER);
    Text text = new Text(shell,SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL);
    text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
    text.setText(s);
    text.pack();
  }

  private void createCloseButton() {
    Button close = new Button(shell, SWT.NONE);
    close.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    close.setText("Close");
    close.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        actionClose();
      }
    });    
  }

  private void actionClose() {
    shell.dispose();
    shell = null;
  }
}
