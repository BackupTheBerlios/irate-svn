// Copyright 2003 Stephen Blackheath

package irate.plugin;

import irate.common.Track;

/**
 * Base class for all plugins.
 *
 * @author Stephen Blackheath
 */
public abstract class Plugin
{
  private PluginApplication app;

  /**
   * Plugin identifier, used to map to the right configurator.
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
}

