// Copyright 2003 Anthony Jones

package irate.swt;

import irate.common.Version;

import java.io.IOException;
import java.io.Reader;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

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
      createDialog();
      Composite mainComposite = dialog.getMainComposite();
      createHeaderGrid(mainComposite);
      Label label = createText(mainComposite, reader);
      GridData data = new GridData(GridData.FILL_HORIZONTAL
                                   | GridData.FILL_VERTICAL);
//      final int numberLinesShown = 10;
//      data.heightHint = numberLinesShown * text.getLineHeight();;
//      text.setLayoutData(data);
      createCloseButton();
      dialog.getShell().pack();
      dialog.centerOn(parent);
      dialog.getShell().open();
    }
  }

  private void createDialog() {
    String title =
      getResourceString("AboutDialog.Title") + " "
      + getResourceString("titlebar.program_name");
    dialog = new BaseDialog(display, title);
    dialog.getMainComposite().setLayout(new GridLayout(1, false));
    dialog.getShell().addShellListener(new ShellAdapter() {
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
    try {
      icon = Resources.getIconImage(display, dialog.getShell().getBackground(), 48, 48);
      labelImage.setImage(icon);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    Label labelText = new Label(header, SWT.HORIZONTAL);
    String text = getResourceString("titlebar.program_name");
    String version = Version.getVersionString();
    if (! version.equals(""))
      text = text + " " + version;
    labelText.setText(text);
    labelText.setFont(createBoldFont(labelText.getFont()));
    return header;

  }

  private Label createText(Composite parent, Reader reader) {
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

  private Label createText(Composite parent, String s) {
    Label label = new Label(parent, SWT.NONE);
    label.setText(s);
    return label;
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
    dialog.dispose();
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
