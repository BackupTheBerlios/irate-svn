// Copyright 2003 Stephen Blackheath

package irate.plugin;

import java.io.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import nanoxml.*;

import irate.plugin.lircremote.LircRemoteControlPlugin;

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
    docElt.parseFromReader(new InputStreamReader(is));
    
    Enumeration enum = docElt.enumerateChildren();
    while(enum.hasMoreElements()) {
      XMLElement elt = (XMLElement)enum.nextElement();
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
    XMLElement docElt = new XMLElement(new Hashtable(), false, false);
    docElt.setName("irate");
    for (int i = 0; i < plugins.size(); i++) {
      Plugin plugin = (Plugin) plugins.get(i);
      XMLElement elt = new XMLElement(new Hashtable(), false, false);
      elt.setName("plugin");
      docElt.addChild(elt);
      elt.setAttribute("id", plugin.getIdentifier());
      elt.setAttribute("attached", plugin.isAttached()?"true":"false");
      plugin.formatConfig(elt);
    }
    FileWriter fw = new FileWriter(getConfigFile());
    try {
      fw.write("<?xml version=\"1.0\"?>\n");
      fw.write(docElt.toString());
      fw.write("\n");
    }
    finally {
      fw.close();
    }
  }
}

