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
	public static final int BROWSER_PAGE = 1;	
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
    shell.dispose();

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
		GridLayout layout = new GridLayout(1, false);
    shell.setLayout(layout);
    
    tabs = new TabFolder(shell, SWT.NONE);
		//Pluginpage
    TabItem tabItem = new TabItem(tabs, SWT.NONE);
    tabItem.setText("Plugins");
		tabItem.setControl(createPluginPage(tabs));
    tabItem = new TabItem(tabs, SWT.NONE);
    tabItem.setText("Browser");
		tabItem.setControl(createBrowserPage(tabs));
		tabs.pack();

    Button ok = new Button(shell, SWT.NONE);
    ok.setText("Close");
    
    //gd.horizontalSpan = 2;
    ok.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
    ok.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e){
        shell.close();
      }
    });
		
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

		return comp;
	}

  
	private Composite createBrowserPage(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout(1, false);
    comp.setLayout(layout);
    new Label(comp, SWT.NONE).setText("Browser command(%url will be replaced with the address)");
    Text t = new Text(comp, SWT.NONE);
    t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    return comp;
  }
}
