// Copyright 2003 Anthony Jones

package irate.swt;

import java.io.Reader;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class ErrorDialog {
  
  private Display display;

  private BaseDialog dialog;

  private Shell parent;

  public ErrorDialog(Display display, Shell parent) {
    this.display = display;
    this.parent = parent;
  }

  /** Display an error message */
  public void show(Reader r) {
    if (dialog == null) {
      createDialog();
      createText(r);
      createCloseButton();
      dialog.getShell().pack();
      dialog.centerOn(parent);
      dialog.getShell().open();
    }
  }
  
  /** Sets the silly parent */
  public void setParent(Shell parent) {
    this.parent = parent;
  }
  
  private void createDialog() {
    dialog = new BaseDialog(display, "");
    GridLayout layout = new GridLayout(1, false);
    dialog.getMainComposite().setLayout(layout);
    dialog.getShell().addShellListener(new ShellAdapter() {
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
    Text text = new Text(dialog.getMainComposite(),
                         SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL);
    GridData data =
        new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
    final int numberLinesShown = 10;
    data.heightHint = numberLinesShown * text.getLineHeight();;
    text.setLayoutData(data);
    text.setText(s);
  }

  private void createCloseButton() {
    Button close =
        dialog.addButton(getResourceString("ErrorDialog.Button.Close")); 
    close.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        actionClose();
      }
    });    
  }

  private void actionClose() {
    dialog.dispose();
    dialog = null;
  }
  
  /**
   * Get a resource string from the properties file associated with this 
   * class.
   */
  private String getResourceString(String key) {
    return Resources.getString(key); 
  }
  
}
