// Copyright 2003 Stephen Blackheath

package irate.plugin;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

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

      // Cheat just to get it working for now...
    ((Plugin)plugins.get(0)).attach(app);
  }

  /**
   * This may be extended to load plugins from files, or wherever, but currently
   * it's hard-coded.
   */
  private void loadPlugins()
  {
    plugins = new Vector();
    plugins.add(new LircRemoteControlPlugin());
  }
}

