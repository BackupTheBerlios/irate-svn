package irate.swt;

import irate.common.Preferences;
import irate.common.TrackDatabase;
import irate.download.DownloadThread;
import irate.resources.Resources;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;


/**
 * 
 * Date Created: Jun 19, 2003
 * Date Updated: $Date: 2003/12/01 03:48:28 $
 * @author Creator:	taras
 * @author Updated:	$Author: parlabane $
 * @version $Revision: 1.15 $
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

    shell.addShellListener(new ShellAdapter() {
      public void shellClosed(ShellEvent e) {
        done = true;
      }
    });

    shell.setText(getResourceString( "AccountDialog.Title.Account_Settings"));  
    GridLayout layout = new GridLayout(3, false);
    shell.setLayout(layout);

//    new Label(shell, SWT.NONE).setText(
//      "Please choose a User name and password. If\n"
//        + "the User does not exist it will be created.");
  
    createUserInfo();

    new Label(shell, SWT.NONE).setText(getResourceString("AccountDialog.Label.Password"));  
    txtPassword = new Text(shell, SWT.SINGLE | SWT.BORDER);
    GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
    data.horizontalSpan = 2;
    txtPassword.setLayoutData(data);String password = trackDatabase.getPassword();
    if (password.length() == 0)
      password = randomString(10);
    txtPassword.setText(password);
    txtPassword.setToolTipText(getResourceString("AccountDialog.Text.Tooltip.Password")); 

    new Label(shell, SWT.NONE).setText(getResourceString("AccountDialog.Label.Server"));  
    txtServer = new Text(shell, SWT.SINGLE | SWT.BORDER);
    data = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
    data.horizontalSpan = 2;
    txtServer.setLayoutData(data);
    txtServer.setText(trackDatabase.getHost());
    txtServer.setToolTipText(getResourceString("AccountDialog.Text.Tooltip.Server"));  

    new Label(shell, SWT.NONE).setText(getResourceString("AccountDialog.Label.Port"));  
    txtPort = new Text(shell, SWT.SINGLE | SWT.BORDER);
    data = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
    data.horizontalSpan = 2;
    txtPort.setLayoutData(data);
    txtPort.setText(Integer.toString(trackDatabase.getPort()));
    txtPort.setToolTipText(getResourceString("AccountDialog.Text.Tooltip.Port"));  
    
    new Label(shell, SWT.NONE).setText(getResourceString("AccountDialog.Label.Directory"));  
    txtDirectory = new Text(shell, SWT.SINGLE | SWT.BORDER);
    data = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
    data.horizontalSpan = 1;
    data.widthHint = 200;
    txtDirectory.setEnabled(false);
    txtDirectory.setLayoutData(data);
    txtDirectory.setText(System.getProperties().getProperty("user.home"));  
    txtDirectory.setToolTipText(getResourceString("AccountDialog.Text.Tooltip.Directory"));  

    Button btnDirectory = new Button(shell, SWT.NONE);
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
    
    Button btnCancel = new Button(shell, SWT.NONE);
    btnCancel.setText(getResourceString("AccountDialog.Button.Cancel"));  
    btnCancel.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        done = true;
      }
    });
    createAcceptButton();

    shell.addShellListener(new ShellAdapter() {
      public void shellClosed(ShellEvent e) {
        done = true;
      }
    });

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

  /** Create the user input field. */
  private void createUserInfo() {
    new Label(shell, SWT.NONE).setText(getResourceString("AccountDialog.Label.User"));  
    txtUser = new Text(shell, SWT.SINGLE | SWT.BORDER);
    GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
    data.horizontalSpan = 2;
    txtUser.setLayoutData(data);String userName = trackDatabase.getUserName();
    if (userName.length() == 0) 
      userName = System.getProperty("user.name");// + " " + randomString(3);   
    txtUser.setText(userName);
    txtUser.setToolTipText(getResourceString("AccountDialog.Text.ToolTip.User"));  
  }
 
  /** Create the OK button. */
  private void createAcceptButton() {
    Button btnAccept = new Button(shell, SWT.NONE);
    btnAccept.setText(getResourceString("AccountDialog.Button.OK"));  
  
    btnAccept.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        trackDatabase.setUserName(txtUser.getText());
        trackDatabase.setPassword(txtPassword.getText());
        trackDatabase.setHost(txtServer.getText());
        trackDatabase.setPort(Integer.parseInt(txtPort.getText()));
        
         // Check the current directory for an existing trackdatabase.xml for
         // compatibility reasons only.
         File dir = new File(".");  
    
         File file = new File(dir, "trackdatabase.xml");  
         if (!file.exists()) {
          dir = new File("/irate");  
          file = new File(dir, "trackdatabase.xml");  
          dir = new File(txtDirectory.getText(), "irate");  
          
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
        
        downloadThread.contactServer(trackDatabase);
        if (trackDatabase.getNoOfTracks() != 0) {
          done = true;
          success = true;
          trackDatabase.setAutoDownload(5);
        }
      }
    });
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
    return Resources.getString(this.getClass().getPackage().getName() + ".locale", key); 
  }
  
}
