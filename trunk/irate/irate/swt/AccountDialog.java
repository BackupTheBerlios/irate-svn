package irate.swt;

import java.util.Random;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.*;
//import org.eclipse.swt.graphics.*;
import org.eclipse.swt.SWT;
import irate.common.*;
import irate.download.DownloadThread;


/**
 * 
 * Date Created: Jun 19, 2003
 * Date Updated: $Date: 2003/09/26 17:18:30 $
 * @author Creator:	taras
 * @author Updated:	$Author: ajones $
 * @version $Revision: 1.8 $
 */
public class AccountDialog {
  boolean done = false;

  private Shell shell;
  private TrackDatabase trackDatabase;
  private DownloadThread downloadThread;
  private Random random = new Random();
  private Text txtUser;
  private Text txtPassword;
  private Text txtServer;
  private Text txtPort;
  /**
   * Creates a new AccountDialog class.
   * 
   * @param display
   * @param trackDatabase
   */
  public AccountDialog(Display display, TrackDatabase trackDatabase, DownloadThread downloadThread) {
    this.shell = new Shell(display);
    this.trackDatabase = trackDatabase;

    shell.addShellListener(new ShellAdapter() {
      public void shellClosed(ShellEvent e) {
        done = true;
      }
    });

    shell.setText("Account Settings");
    GridLayout layout = new GridLayout(2, false);
    shell.setLayout(layout);

//    new Label(shell, SWT.NONE).setText(
//      "Please choose a User name and password. If\n"
//        + "the User does not exist it will be created.");
  
    createUserInfo();

    new Label(shell, SWT.NONE).setText("Password");
    final Text txtPassword = new Text(shell, SWT.SINGLE | SWT.BORDER);
    txtPassword.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
    String password = trackDatabase.getPassword();
    if (password.length() == 0)
      password = randomString(10);
    txtPassword.setText(password);

    new Label(shell, SWT.NONE).setText("Server");
    txtServer = new Text(shell, SWT.SINGLE | SWT.BORDER);
    txtServer.setLayoutData(
      new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
    txtServer.setText(trackDatabase.getHost());

    new Label(shell, SWT.NONE).setText("Port");
    txtPort = new Text(shell, SWT.SINGLE | SWT.BORDER);
    txtPort.setLayoutData(
      new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
    txtPort.setText(Integer.toString(trackDatabase.getPort()));

    createAcceptButton();

    Button btnCancel = new Button(shell, SWT.NONE);
    btnCancel.setText("Cancel");
    btnCancel.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        done = true;
        System.exit(0);
      }
    });

    shell.addShellListener(new ShellAdapter() {
      public void shellClosed(ShellEvent e) {
        done = true;
        System.exit(0);
      }
    });

    shell.pack();
    shell.open();
    while (!done) {
      if (!display.readAndDispatch())
        display.sleep();
    }
    shell.close();
    shell.dispose();
  }

  /** Create the user input field. */
  private void createUserInfo() {
    new Label(shell, SWT.NONE).setText("User");
    txtUser = new Text(shell, SWT.SINGLE | SWT.BORDER);
    txtUser.setLayoutData(
      new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
    String userName = trackDatabase.getUserName();
    if (userName.length() == 0) 
      userName = System.getProperty("user.name") + " " + randomString(3); 
    txtUser.setText(userName);
  }
 
  /** Create the OK button. */
  private void createAcceptButton() {
    Button btnAccept = new Button(shell, SWT.NONE);
    btnAccept.setText("OK");
  
    btnAccept.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        trackDatabase.setUserName(txtUser.getText());
        trackDatabase.setPassword(txtPassword.getText());
        trackDatabase.setHost(txtServer.getText());
        trackDatabase.setPort(Integer.parseInt(txtPort.getText()));
        downloadThread.contactServer(trackDatabase);
        if (trackDatabase.getNoOfTracks() != 0)
          done = true;
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
}
