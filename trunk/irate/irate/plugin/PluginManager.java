// Copyright 2003 Stephen Blackheath

package irate.plugin;

import java.io.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import nanoxml.*;

import irate.common.Preferences;
import irate.plugin.lircremote.LircRemoteControlPlugin;
import irate.plugin.externalcontrol.ExternalControlPlugin;
import irate.plugin.unratednotifier.UnratedNotifierPlugin;

/**
 * Manager to manage the list of plugins.
 *
 * @author Stephen Blackheath
 */
public class PluginManager
{
  private PluginApplication app;
  private File configDir;

  private Vector plugins;

  public PluginManager(PluginApplication app, File configDir)
  {
    this.app = app;
    this.configDir = configDir;

    loadPlugins();
  }

  public PluginApplication getApp()
  {
    return app;
  }

  public List getPlugins()
  {
    return plugins;
  }

  /**
   * This may be extended to load plugins from files, or wherever, but currently
   * it's hard-coded.
   */
  private void loadPlugins()
  {
    plugins = new Vector();
    plugins.add(new LircRemoteControlPlugin());
    plugins.add(new ExternalControlPlugin());
    plugins.add(new UnratedNotifierPlugin());
    try {
      loadConfig();
    }
    catch (IOException e) {
    }
  }

  private File getConfigFile()
  {
    return new File(configDir, "irate.xml");
  }

  private File getConfigFileTemporary()
  {
    return new File(configDir, "irate.xml~");
  }

  private void loadConfig()
    throws IOException
  {
    FileInputStream fis = new FileInputStream(getConfigFile());
    loadConfig(new FileInputStream(getConfigFile()));
  }

  private void loadConfig(InputStream is)
    throws IOException
  {
    XMLElement docElt = new XMLElement(new Hashtable(), false, false);
    
    InputStreamReader inputStreamReader = new InputStreamReader(is);
    docElt.parseFromReader(inputStreamReader);
    inputStreamReader.close();
    is.close();
    
    Enumeration e = docElt.enumerateChildren();
    while(e.hasMoreElements()) {
      XMLElement elt = (XMLElement)e.nextElement();
      if (elt.getName().equals("plugin")) {
        String identifier = elt.getStringAttribute("id");
        if (identifier != null)
          for (int i = 0; i < plugins.size(); i++) {
            Plugin plugin = (Plugin) plugins.get(i);
            if (plugin.getIdentifier().equals(identifier)) {
              plugin.parseConfig(elt);
              String attached = elt.getStringAttribute("attached");
              if (attached != null && attached.equals("true"))
                plugin.attach(app);
              break;
            }
          }
      }
    }
  }

  /**
   * Save the configuration of all the plugins.
   */
  public void saveConfig()
    throws IOException
  {
    //XMLElement docElt = new XMLElement(new Hashtable(), false, false);
    //docElt.setName("irate");
    for (int i = 0; i < plugins.size(); i++) {
      Plugin plugin = (Plugin) plugins.get(i);
      XMLElement elt = new XMLElement(new Hashtable(), false, false);
      elt.setName("plugin");
      elt.setAttribute("id", plugin.getIdentifier());
      elt.setAttribute("attached", plugin.isAttached()?"true":"false");
      plugin.formatConfig(elt);
      Preferences.updateWithChild(elt);
    }

  }
  
}

