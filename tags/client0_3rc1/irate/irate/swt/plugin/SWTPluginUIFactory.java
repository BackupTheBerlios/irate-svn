// Copyright 2003 Stephen Blackheath

package irate.swt.plugin;

import irate.plugin.Plugin;
import irate.plugin.PluginApplication;
import irate.plugin.PluginUIFactory;
import org.eclipse.swt.widgets.Display;

/**
 * Base class for factory that finds user interface objects for plugins.
 * Subclasses are for specific user-interface implementations.
 *
 * @author Stephen Blackheath
 */
public class SWTPluginUIFactory
  extends PluginUIFactory
{
  private Display display;
  private PluginApplication app;

  public SWTPluginUIFactory(Display display, PluginApplication app)
  {
    this.display = display;
    this.app = app;
  }

  /**
   * Look up a UI object based on the class of the plugin and the specified
   * type of object required.  The implementation should pass the plugin
   * instance to the object.
   * 
   * @return null if a UI object can't be found for it.
   */
  public Object lookup(Plugin plugin, String type)
  {
    // Note: These names are chosen so we can later switch to an implementation
    // that automatically converts
    //   irate.plugin.lircremote.LircRemoteControlPlugin
    // to
    //   irate.swt.plugin.lircremote.LircRemoteControlConfigurator
    // (by adding 'swt' before 'plugin' and type in place of 'Plugin')
    //
    // If we stick to this convention, then we can later support plugins that
    // are downloaded automatically.
    //
    if (type.equals(CONFIGURATOR)) {
      if (plugin instanceof irate.plugin.lircremote.LircRemoteControlPlugin)
        return new irate.swt.plugin.lircremote.LircRemoteControlConfigurator(display, app, plugin);
      if (plugin instanceof irate.plugin.externalcontrol.ExternalControlPlugin)
        return new irate.swt.plugin.externalcontrol.ExternalControlConfigurator(display, app, plugin);
    }
    return null;
  }
}

