// Copyright 2003 Anthony Jones

package irate.swt;

import irate.resources.BaseResources;
import java.io.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class AboutDialog {

  private Display display;

  private Shell shell;

  private Shell parent;

  private Image icon;

  public AboutDialog(Display display, Shell parent) {
    this.display = display;
    this.parent = parent;
  }

  public void show(Reader reader) {
    if (shell == null) {
      createIcon();
      createShell();
      createHeaderGrid(shell);
      Text text = createText(shell, reader);
      GridData data = new GridData(GridData.FILL_HORIZONTAL
                                   | GridData.FILL_VERTICAL);
      final int numberLinesShown = 10;
      data.heightHint = numberLinesShown * text.getLineHeight();;
      text.setLayoutData(data);
      createCloseButton(shell);      
      shell.pack();
      shell.open();
    }
  }

  private void createIcon() {
    icon = null;
    try {
      InputStream stream = BaseResources.getResourceAsStream("icon.gif");
      ImageData imageData = new ImageData(stream); 
      int whitePixel = imageData.palette.getPixel(new RGB(255, 255, 255));
      imageData.transparentPixel = whitePixel;
      icon = new Image(display, imageData.scaledTo(32, 32));
    }
    catch (IOException e) {
    }
  }
  
  private void createShell() {
    shell = new Shell(display);
    GridLayout layout = new GridLayout(1, false);
    shell.setLayout(layout);
    shell.setText(getResourceString("AboutDialog.Title") + " "
                  + getResourceString("titlebar.program_name"));
    if (icon != null)
      shell.setImage(icon);
    shell.addShellListener(new ShellAdapter() {
      public void shellClosed(ShellEvent e){
        actionClose();
      }
    });
  }

  private Composite createHeaderGrid(Composite parent) {
    Composite header = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout();
    layout.numColumns = 2;
    header.setLayout(layout);
    Label labelImage = new Label(header, SWT.HORIZONTAL);
    if (icon != null)
      labelImage.setImage(icon);
    Label labelText = new Label(header, SWT.HORIZONTAL);
    labelText.setText(getResourceString("titlebar.program_name"));
    labelText.setFont(createBoldFont(labelText.getFont()));
    return header;

  }

  private Text createText(Composite parent, Reader reader) {
    try {
      StringBuffer sb = new StringBuffer();      
      char[] buf = new char[512];
      int nbytes;
      while ((nbytes = reader.read(buf, 0, buf.length)) != -1) 
        sb.append(new String(buf, 0, nbytes));
      return createText(parent, sb.toString());
    }
    catch (Exception e) {
      e.printStackTrace();
      return createText(parent, e.toString());
    }
  }

  private Text createText(Composite parent, String s) {
    Text text = new Text(parent, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP
                         | SWT.V_SCROLL);
    text.setText(s);
    return text;
  }

  private void createCloseButton(Composite parent) {
    Button button = new Button(parent, SWT.NONE);
    GridData data = new GridData();
    data.horizontalAlignment = GridData.END;
    data.grabExcessHorizontalSpace = true;
    button.setLayoutData(data);
    button.setText(getResourceString("ErrorDialog.Button.Close")); 
    button.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        actionClose();
      }
    });    
  }

  private Font createBoldFont(Font font) {
    FontData[] data = font.getFontData();
    for (int i = 0; i < data.length; i++) {
      data[i].setStyle(SWT.BOLD);
    }
    return(new Font(display, data));
  }

  private void actionClose() {
    shell.dispose();
    shell = null;
  }

  private String getResourceString(String key) {
    return Resources.getString(key); 
  }
  
}

// Local Variables:
// c-file-style:"gnu"
// c-basic-offset:2
// indent-tabs-mode:nil
// tab-width:4
// End:
