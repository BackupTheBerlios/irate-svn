/*
 * Created on Jun 19, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package irate.swt;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.*;
//import org.eclipse.swt.graphics.*;
import org.eclipse.swt.SWT;
import irate.common.*;
/**
 * @author taras
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class AccountDialog {
	
	public AccountDialog(Display display, TrackDatabase trackDatabase)
	{
		final Shell shell = new Shell(display);
		shell.setText("Account Settings");
		GridLayout layout = new GridLayout(2, false);
		shell.setLayout(layout);
		
		new Label(shell, SWT.NONE).setText("User");
		final Text txtUser = new Text(shell, SWT.SINGLE | SWT.BORDER);
		txtUser.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));;
		txtUser.setText(trackDatabase.getUserName());
		
		new Label(shell, SWT.NONE).setText("Password");
		final Text txtPassword = new Text(shell,SWT.SINGLE | SWT.BORDER);
		txtPassword.setLayoutData(new GridData(GridData.FILL_HORIZONTAL| GridData.FILL_VERTICAL));;
		txtPassword.setText(trackDatabase.getPassword());
		
		new Label(shell, SWT.NONE).setText("Host");
		final Text txtHost = new Text(shell, SWT.SINGLE | SWT.BORDER);
		txtHost.setLayoutData(new GridData(GridData.FILL_HORIZONTAL| GridData.FILL_VERTICAL));
		txtHost.setText(trackDatabase.getHost());
		
		new Label(shell, SWT.NONE).setText("Port");
		final Text txtPort = new Text(shell,SWT.SINGLE | SWT.BORDER);
		txtPort.setLayoutData(new GridData(GridData.FILL_HORIZONTAL| GridData.FILL_VERTICAL));
		txtPort.setText(""+trackDatabase.getPort());
		
		Button btnSave = new Button(shell, SWT.NONE);
		btnSave.setText("Save");
		
		final TrackDatabase finTrackDatabase  = trackDatabase;
		btnSave.addSelectionListener(new SelectionAdapter(){
			  public void widgetSelected(SelectionEvent e){
			  	finTrackDatabase.setUserName(txtUser.getText());
			  	finTrackDatabase.setPassword(txtPassword.getText());
			  	finTrackDatabase.setHost(txtHost.getText());
			  	finTrackDatabase.setPort(Integer.parseInt(txtPort.getText()));
			  	//shell.close();
			  }
			});    
		
		Button btnCancel = new Button(shell, SWT.NONE);
		btnCancel.setText("Cancel");
		btnSave.addSelectionListener(new SelectionAdapter(){
		  public void widgetSelected(SelectionEvent e){
			if(finTrackDatabase.getUserName().length() == 0 || finTrackDatabase.getPassword().length() == 0)
				System.exit(0);
			shell.close();
		  }
		});    
		
		shell.pack();    
		shell.open();
	}
	
	
}
