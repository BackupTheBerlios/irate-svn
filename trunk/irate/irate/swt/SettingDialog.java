// Copyright 2003 Stephen Blackheath

package irate.swt;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.SWT;
import java.io.IOException;
import irate.plugin.*;

/**
 * @author Stephen Blackheath
 */
public class SettingDialog
{
	public static final int PLUGIN_PAGE = 0;
	
  private PluginManager pluginManager;
  private boolean done = false;
	private Shell shell;
  private PluginApplication app;
  private TabFolder tabs;
    
	public SettingDialog(Display display, PluginManager pluginManager, PluginApplication app) {
    this.pluginManager = pluginManager;
		this.app = app;
    
    createWidgets(display);
    
  }
  
  /** Ask for a specific setting */
  public void setPage(int page) {
    tabs.setSelection(page);
  }
  
  /** Show prefs window 
  @param display swt display */
  public void open(Display display) {
    shell.open();
    while (!done) {
      if (!display.readAndDispatch()) display.sleep();
    }
    try {
      pluginManager.saveConfig();
    }
    catch (IOException e) {
        // @todo Handle error better
      e.printStackTrace();
    }
    //should do dispose in a finalyzer
    //shell.dispose();

  }
	/** Creates widgets */
	private void createWidgets(Display display) {
    shell = new Shell(display);
    shell.setText("Settings");
    shell.addShellListener(new ShellAdapter() {
      public void shellClosed(ShellEvent e){
        done=true;
      }
    });
		tabs = new TabFolder(shell, SWT.NONE);
		//Pluginpage
    TabItem tabItem = new TabItem(tabs, SWT.NONE);
    tabItem.setText("Plugins");
		tabItem.setControl(createPluginPage(tabs));
		tabs.pack();
		
    shell.pack();    
  }
  
	private Composite createPluginPage(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout(2, false);
    comp.setLayout(layout);

    Label heading = new Label(comp, SWT.NONE);
    heading.setText("Select the plugins you wish to enable");
    GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
    gd.horizontalSpan = 2;
    heading.setLayoutData(gd);

    java.util.List plugins = pluginManager.getPlugins();
    for (int i = 0; i < plugins.size(); i++) {
      final Plugin plugin = (Plugin) plugins.get(i);
      final Button checkbox = new Button(comp, SWT.CHECK);
      checkbox.setText(plugin.getDescription());
      checkbox.setSelection(plugin.isAttached());
      checkbox.addSelectionListener(new SelectionAdapter(){
				public void widgetSelected(SelectionEvent e){
					if (checkbox.getSelection())
						plugin.attach(pluginManager.getApp());
								else
						plugin.detach();
				}
      });
      Button configure = new Button(comp, SWT.NONE);
      configure.setText("Configure");
      configure.addSelectionListener(new SelectionAdapter(){
        public void widgetSelected(SelectionEvent e){
          app.getUIFactory().lookup(plugin, PluginUIFactory.CONFIGURATOR);
        }
			});
		}

    Button ok = new Button(comp, SWT.NONE);
    ok.setText("OK");
    gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
    gd.horizontalSpan = 2;
    ok.setLayoutData(gd);
    ok.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e){
        shell.close();
      }
    });
		return comp;
	}
}
