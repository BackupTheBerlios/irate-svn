package irate.swt;

import irate.common.Preferences;
import irate.common.TrackDatabase;
import irate.download.DownloadThread;
import irate.resources.BaseResources;
import irate.common.UpdateListener;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.Random;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;

/**
 * 
 * Date Created: Jun 19, 2003
 * Date Updated: $Date: 2004/01/16 22:15:41 $
 * @author Creator:	taras
 * @author Updated:	$Author: ajones $
 * @version $Revision: 1.20 $
 */
public class AccountDialog {
  private boolean done = false;
  private boolean success = false;

  private Shell shell;
  private TrackDatabase trackDatabase;
  private DownloadThread downloadThread;
  private Random random = new Random();
  private Text txtUser;
  private Text txtPassword;
  private Text txtServer;
  private Text txtPort;
  private Text txtDirectory;
  private Button buttonDirectorySelect;
  private Display display;
  private TabFolder tabs;
  private Label lblStatus;
  private Button btnAccept;
  /**
   * Creates a new AccountDialog class.
   * 
   * @param display
   * @param trackDatabase
   */
  public AccountDialog(Display display, TrackDatabase trackDatabase, DownloadThread downloadThread) {
    this.shell = new Shell(display);
    this.trackDatabase = trackDatabase;
    this.downloadThread = downloadThread;
    this.display = display;

    shell.setImage(Resources.getIconImage(display));
    shell.setText(getResourceString( "AccountDialog.Title.Account_Settings"));
    shell.addShellListener(new ShellAdapter() {
      public void shellClosed(ShellEvent e) {
        done = true;
      }
    });
  
    shell.setLayout(new GridLayout(1, false));
    tabs = new TabFolder(shell, SWT.NONE);
    TabItem tabItem = new TabItem(tabs, SWT.NONE);
    tabItem.setText("Introduction");
    Composite composite = new Composite(tabs, SWT.NONE);
    tabItem.setControl(composite);
    composite.setLayout(new GridLayout(2, false));
    Label lblIcon = new Label(composite, SWT.NONE);
    lblIcon.setImage(Resources.getIconImage(display));
    Text txt = new Text(composite, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP);
//    txt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL| GridData.FILL_VERTICAL));
    txt.setText(getResourceString("AccountDialog.Intro"));
    
    tabItem = new TabItem(tabs, SWT.NONE);
    tabItem.setText("Account Settings");
    composite = new Composite(tabs, SWT.NONE);
    txt = new Text(composite, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP);
    GridData data = new GridData(GridData.FILL_HORIZONTAL| GridData.FILL_VERTICAL);
    data.horizontalSpan = 4;
    txt.setLayoutData(data);
    txt.setText(getResourceString("AccountDialog.Settings"));

    createMain(composite);
    tabItem.setControl(composite);
    
    tabItem = new TabItem(tabs, SWT.NONE);
    tabItem.setText("Server Communication");
    composite = new Composite(tabs, SWT.NONE);
    tabItem.setControl(composite);
    createStatus(composite);
  
    
    Composite buttonComposite = new Composite(shell, SWT.NONE);
    buttonComposite.setLayout(new RowLayout(SWT.HORIZONTAL));
    buttonComposite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
  
    Button btnCancel = new Button(buttonComposite, SWT.NONE);
    btnCancel.setText(getResourceString("AccountDialog.Button.Cancel"));  
    btnCancel.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        done = true;
      }
    });
    createAcceptButton(buttonComposite);
    
    tabs.pack();
    shell.pack();
    Point size = shell.getSize();
    if (size.x < 320)
      size.x = 320;
    shell.setSize(size);
//    shell.setLocation();
    
    shell.open();
    while (!done) {
      if (!display.readAndDispatch())
        display.sleep();
    }
    shell.close();
    shell.dispose();
    if (!success)
      System.exit(0);
  }

  /** create the main tab */
  private void createMain(Composite mainComposite) {
//    new Label(mainComposite, SWT.NONE).setText(
//      "Please choose a User name and password. If\n"
//        + "the User does not exist it will be created.");
    GridLayout layout = new GridLayout(3, false);
    mainComposite.setLayout(layout);

    createUserInfo(mainComposite);

    new Label(mainComposite, SWT.NONE).setText(getResourceString("AccountDialog.Label.Password"));  
    txtPassword = new Text(mainComposite, SWT.SINGLE | SWT.BORDER);
    GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
    data.horizontalSpan = 2;
    txtPassword.setLayoutData(data);String password = trackDatabase.getPassword();
    if (password.length() == 0)
      password = randomString(10);
    txtPassword.setText(password);
    txtPassword.setToolTipText(getResourceString("AccountDialog.Text.Tooltip.Password")); 

    new Label(mainComposite, SWT.NONE).setText(getResourceString("AccountDialog.Label.Server"));  
    txtServer = new Text(mainComposite, SWT.SINGLE | SWT.BORDER);
    data = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
    data.horizontalSpan = 2;
    txtServer.setLayoutData(data);
    txtServer.setText(trackDatabase.getHost());
    txtServer.setToolTipText(getResourceString("AccountDialog.Text.Tooltip.Server"));  

    new Label(mainComposite, SWT.NONE).setText(getResourceString("AccountDialog.Label.Port"));  
    txtPort = new Text(mainComposite, SWT.SINGLE | SWT.BORDER);
    data = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
    data.horizontalSpan = 2;
    txtPort.setLayoutData(data);
    txtPort.setText(Integer.toString(trackDatabase.getPort()));
    txtPort.setToolTipText(getResourceString("AccountDialog.Text.Tooltip.Port"));  
    
    new Label(mainComposite, SWT.NONE).setText(getResourceString("AccountDialog.Label.Directory"));  
    txtDirectory = new Text(mainComposite, SWT.SINGLE | SWT.BORDER);
    data = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
    data.horizontalSpan = 1;
    data.widthHint = 200;
    txtDirectory.setEnabled(false);
    txtDirectory.setLayoutData(data);
    txtDirectory.setText(System.getProperties().getProperty("user.home"));  
    txtDirectory.setToolTipText(getResourceString("AccountDialog.Text.Tooltip.Directory"));  

    Button btnDirectory = new Button(mainComposite, SWT.NONE);
    btnDirectory.setText(getResourceString("AccountDialog.Button.Browse"));  
    btnDirectory.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        DirectoryDialog dialog = new DirectoryDialog (shell);
        dialog.setMessage(getResourceString("AccountDialog.Dialog.Message"));  
        dialog.setText(getResourceString("AccountDialog.Dialog.Download_Directory"));  
        String result = dialog.open();
        if(result != null) {
          txtDirectory.setText(result);
        }
      }
    });
  }
    
  private void createStatus(Composite composite) {
    composite.setLayout(new GridLayout(2, false));
    Label lblIcon = new Label(composite, SWT.NONE);
    lblIcon.setImage(Resources.getIconImage(display));
    lblStatus = new Label(composite, SWT.NONE);
    lblStatus.setText("status goes here");
  }
  /** Create the user input field. */
  private void createUserInfo(Composite parent) {
    new Label(parent, SWT.NONE).setText(getResourceString("AccountDialog.Label.User"));  
    txtUser = new Text(parent, SWT.SINGLE | SWT.BORDER);
    GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
    data.horizontalSpan = 2;
    txtUser.setLayoutData(data);String userName = trackDatabase.getUserName();
    if (userName.length() == 0) 
      userName = System.getProperty("user.name");// + " " + randomString(3);   
    txtUser.setText(userName);
    txtUser.setToolTipText(getResourceString("AccountDialog.Text.ToolTip.User"));  
  }
 
  /** Create the OK button. */
  private void createAcceptButton(Composite parent) {
    btnAccept = new Button(parent, SWT.NONE);
    btnAccept.setText(getResourceString("AccountDialog.Button.OK"));  
  
    btnAccept.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        int index = tabs.getSelectionIndex();
        index++;
        tabs.setSelection(index);
        if(index==2)
          createAccount();
      }
    });
  }

  private void createAccount() {
    //do the gui bits
    btnAccept.setEnabled(false);
    trackDatabase.setUserName(txtUser.getText());
    trackDatabase.setPassword(txtPassword.getText());
    trackDatabase.setHost(txtServer.getText());
    trackDatabase.setPort(Integer.parseInt(txtPort.getText()));
    final String txtDirText = txtDirectory.getText();
    
    new Thread(new Runnable(){
      public void run() {
      
      
       // Check the current directory for an existing trackdatabase.xml for
       // compatibility reasons only.
       File dir = new File(".");  
  
       File file = new File(dir, "trackdatabase.xml");  
       if (!file.exists()) {
        dir = new File("/irate");  
        file = new File(dir, "trackdatabase.xml");  
        dir = new File(txtDirText, "irate");  
        
        if (!dir.exists())
          dir.mkdir();
        
        file = new File(dir, "trackdatabase.xml");  
        dir = new File(dir, "download");  
        
        if(!dir.exists())
          dir.mkdir();
       }
        
      
      try {
        Preferences.savePreferenceToFile("downloadDir", file.toString());  
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
      trackDatabase.setFile(file);
      trackDatabase.setDownloadDir(dir);
      
      UpdateListener ul = new UpdateListener() {
        public void actionPerformed() {
          display.syncExec(new Runnable() {
            public void run() {
              lblStatus.setText(downloadThread.getState());
              lblStatus.pack();
            }
          });//inner class
        }//method
      };//ul
      downloadThread.addUpdateListener(ul);

      downloadThread.contactServer(trackDatabase);
      downloadThread.removeUpdateListener(ul);
      //System.out.println("grrr");
      if (trackDatabase.getNoOfTracks() != 0) {
        done = true;
        success = true;
        trackDatabase.setAutoDownload(5);
      }else {
        display.asyncExec(new Runnable() {
          public void run() {
            btnAccept.setEnabled(true);
            tabs.setSelection(1);
          }
        });
      }
    
      }
    }).start();
  }
  /** Create a random string of the given length. */  
  private String randomString(int length) {
    StringBuffer sb = new StringBuffer();
    while (sb.length() < length) {
      int i = random.nextInt(10+26+26);
      char ch;
      if (i < 10)
        ch = (char) (48 + i);
      else
        if (i < 36)
          ch = (char) (65 + i - 10);
        else
          ch = (char) (97 + i - 36);
      sb.append(ch);
    }
    return sb.toString();
  }
  
  /**
   * Get a resource string from the properties file associated with this 
   * class.
   */
  private String getResourceString(String key) {
    return Resources.getString(key); 
  }
  
}
