/*
 * Created on Jun 19, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package irate.swt;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.SWT;
/**
 * @author taras
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class AccountDialog {
	
	public AccountDialog(Display display, irate.common.TrackDatabase trackDatabase)
	{
		Shell shell = new Shell(display);
		shell.setText("Account Settings");
		GridLayout layout = new GridLayout(2, false);
		shell.setLayout(layout);
		
		new Label(shell, SWT.NONE).setText("User");
		Text txtUser = new Text(shell, SWT.SINGLE | SWT.BORDER);
		txtUser.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));;
		txtUser.setText(trackDatabase.getUserName());
		
		new Label(shell, SWT.NONE).setText("Password");
		Text txtPassword = new Text(shell,SWT.SINGLE | SWT.BORDER);
		txtPassword.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));;
		txtPassword.setText(trackDatabase.getPassword());
		
		new Label(shell, SWT.NONE).setText("Host");
		Text txtHost = new Text(shell, SWT.SINGLE | SWT.BORDER);
		txtHost.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtHost.setText(trackDatabase.getHost());
		
		new Label(shell, SWT.NONE).setText("Port");
		Text txtPort = new Text(shell,SWT.SINGLE | SWT.BORDER);
		txtPort.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtPort.setText(""+trackDatabase.getPort());
		
		new Button(shell, SWT.NONE).setText("Save");
		new Button(shell, SWT.NONE).setText("Cancel");
		shell.pack();
		shell.open();
	}
	
	
}
