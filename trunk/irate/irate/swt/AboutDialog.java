// Copyright 2003 Anthony Jones

package irate.swt;

import irate.resources.BaseResources;
import irate.version.Version;
import java.io.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class AboutDialog {

  private Display display;

  private BaseDialog dialog;

  private Shell parent;

  private Image icon;

  public AboutDialog(Display display, Shell parent) {
    this.display = display;
    this.parent = parent;
  }

  public void show(Reader reader) {
    if (dialog == null) {
      createIcon();
      createDialog();
      createHeaderGrid(dialog.mainComposite);
      Text text = createText(dialog.mainComposite, reader);
      GridData data = new GridData(GridData.FILL_HORIZONTAL
                                   | GridData.FILL_VERTICAL);
      final int numberLinesShown = 10;
      data.heightHint = numberLinesShown * text.getLineHeight();;
      text.setLayoutData(data);
      createCloseButton();
      dialog.shell.pack();
      dialog.centerOn(parent);
      dialog.shell.open();
    }
  }

  private void createIcon() {
    icon = null;
    try {
      InputStream stream = BaseResources.getResourceAsStream("icon.gif");
      ImageData imageData = new ImageData(stream); 
      icon = new Image(display, imageData.scaledTo(32, 32));
    }
    catch (IOException e) {
    }
  }
  
  private void createDialog() {
    String title =
      getResourceString("AboutDialog.Title") + " "
      + getResourceString("titlebar.program_name");
    dialog = new BaseDialog(display, title);
    dialog.mainComposite.setLayout(new GridLayout(1, false));
    dialog.shell.addShellListener(new ShellAdapter() {
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
    String text = getResourceString("titlebar.program_name");
    String version = Version.getVersionString();
    if (! version.equals(""))
      text = text + " " + version;
    labelText.setText(text);
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

  private void createCloseButton() {
    Button button =
      dialog.addButton(getResourceString("ErrorDialog.Button.Close"));
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
    dialog.shell.dispose();
    dialog = null;
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
