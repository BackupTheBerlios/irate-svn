package irate.plugin;

import irate.common.*;
import nanoxml.XMLElement;

/**
 * Base class for all plugins.
 *
 * @author Created: Stephen Blackheath
 * @author Updated: Robin Sheat
 */
public abstract class Plugin
{
  private PluginApplication app;

  /**
   * Get a short identifier for this Plugin.
   */
  public abstract String getIdentifier();

  /**
   * Get a short description of this plugin.
   */
  public abstract String getDescription();

  /**
   * @return true if this plugin is currently attached to the application.
   */
  public final boolean isAttached()
  {
    return app != null;
  }

  /**
   * Get a longer description of this plugin, suitable for a tooltip.
   */
  public abstract String getLongDescription();

  /**
   * Get access to the application instance.
   */
  protected final PluginApplication getApp()
  {
    return app;
  }

  /**
   * Attach the plugin to the specified application.
   */
  public final void attach(PluginApplication app)
  {
    if (this.app == null) {
      this.app = app;
      doAttach(); 
    }
  }
  
  /**
   * Subclasses to override to do real work of attaching.
   * Application is available through getApp().
   */
  protected abstract void doAttach();

  /**
   * Detach the plugin from the application.
   */
  public final void detach()
  {
    try {
      if (app != null)
	doDetach();
    }
    finally {
      this.app = null;
    }
  }

  /**
   * Subclasses to override to do real work of detaching.
   * Application is available through getApp().
   */
  protected abstract void doDetach();

  /**
   * Parse the configuration stored in the specified element.
   */
  public abstract void parseConfig(XMLElement elt);

  /**
   * Format the configuration of this plugin by modifying the specified
   * element.
   */
  public abstract void formatConfig(XMLElement elt);
  
  
  /**
   * Feedback functions from client to plugin
   */
  public void eventPositionUpdated(int position, int length) { }
  public void eventNewTrack(Track track) { }
  public void eventRatingApplied(Track ratedTrack, int rating) { }
  
}

