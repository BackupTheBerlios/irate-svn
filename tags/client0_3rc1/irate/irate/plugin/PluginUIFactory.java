// Copyright 2003 Stephen Blackheath

package irate.plugin;

/**
 * Base class for factory that finds user interface objects for plugins.
 * Subclasses are for specific user-interface implementations.
 *
 * @author Stephen Blackheath
 */
public abstract class PluginUIFactory
{
  /**
   * 'type' value to use for all configurators.
   */
  public static final String CONFIGURATOR = "Configurator";

  /**
   * Look up a UI object based on the class of the plugin and the specified
   * type of object required.  The implementation should pass the plugin
   * instance to the object.
   * 
   * @return null if a UI object can't be found for it.
   */
  public abstract Object lookup(Plugin plugin, String type);
}

