// Copyright 2003 Stephen Blackheath

package irate.swt;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.SWT;
import java.io.IOException;

import irate.common.Preferences;
import irate.plugin.*;

/**
 * Date Updated: $Date: 2003/11/27 08:12:26 $
 * @author Creator: Stephen Blackheath
 * @author Updated: Robin Sheat
 * @version $Revision: 1.8 $
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
  
  private String browser;
  
  public SettingDialog(Display display, PluginManager pluginManager, PluginApplication app) {
    this.pluginManager = pluginManager;
    this.app = app;
    browser = Preferences.getUserPreference("browser");
    if (browser == null) {
      browser = "";
    }
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
          try {
            Preferences.savePreferenceToFile("browser", browser);
          } catch (IOException ioe) {
            ioe.printStackTrace();
            shell.close();
          }
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
    final Composite comp = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout(1, false);
    comp.setLayout(layout);
    new Label(comp, SWT.NONE).setText("Browser:");

    class BrowserButton {
      String description; // Button label
      String command;     // Command it generates
      Button button;
    
      public BrowserButton(String d, String c) {
        description = d;
        command = c;
        button = new Button(comp, SWT.RADIO);
        button.setText(description);
      }

      public void addSelectionListener(SelectionListener s) {
        button.addSelectionListener(s);
      }

      public Button getButton() {
        return button;
      }
    
      public String getCommand() {
        return command;
      }
    
      public boolean isCommand(String s) {
        return command.equals(s);
      }
    }

    final BrowserButton[] browsers = { 
      new BrowserButton("Mozilla/Firebird (Linux/UNIX)","mozilla"),
      new BrowserButton("Konqueror (Linux/UNIX)","kfmclient exec"),
      new BrowserButton("Windows Default",
                        "rundll32 url.dll,FileProtocolHandler")};
    final Button browserSpecified = new Button(comp, SWT.RADIO);
    final Text browserText = new Text(comp, SWT.NONE);
    final Button browseButton = new Button(comp, SWT.NONE);

    class DoBrowser implements SelectionListener {
      public void widgetSelected(SelectionEvent e) {
        for (int i=0; i<browsers.length; i++) {
          if (browsers[i].getButton().getSelection()) {
            browserText.setText(browsers[i].getCommand());
          }
        }
        if (browserSpecified.getSelection()) {
          browserText.setEnabled(true);
          browseButton.setEnabled(true);
        } else {
          browserText.setEnabled(false);
          browseButton.setEnabled(false);
        }
      }
    
      public void widgetDefaultSelected(SelectionEvent e) { }
    }

    SelectionListener sel = new DoBrowser();

    // Set the default radio button, based on the browser text. Whilst
    // doing this, we can se the selction listener.
    boolean selected = false;
    for (int i=0;i<browsers.length;i++) {
      browsers[i].addSelectionListener(sel);
      if (browsers[i].getCommand().equals(browser) && !selected) {
        browsers[i].getButton().setSelection(true);
        selected = true;
      }
    }
    if (!selected) {
      browserSpecified.setSelection(true);
      browserText.setEnabled(true);
      browseButton.setEnabled(true);
    } else {
      browserText.setEnabled(false);
      browseButton.setEnabled(false);
    }

    browserSpecified.setText("User Specified:");
    browserSpecified.addSelectionListener(sel);

    browserText.setText(browser);
    browserText.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
    browserText.addModifyListener(new ModifyListener(){
        public void modifyText(ModifyEvent e) {
          browser = browserText.getText();
        }
      });
    
    browseButton.setText("Browse ...");
    
    browseButton.addSelectionListener(new SelectionAdapter(){
        public void widgetSelected(SelectionEvent e){
          FileDialog fd = new FileDialog(shell, SWT.OPEN);
          fd.open();
          browserText.setText(fd.getFilterPath() + "/" + fd.getFileName());
          browser = browserText.getText();
        }
      });
    
    
    return comp;
  }

 
}
