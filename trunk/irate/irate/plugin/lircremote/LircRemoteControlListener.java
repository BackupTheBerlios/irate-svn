// Copyright 2003 Stephen Blackheath

package irate.plugin.lircremote;

import irate.plugin.*;
import irate.common.Track;
import java.util.Hashtable;

/**
 * Event listener interface for plugin for remote control based on lircd (Linux/Unix).
 *
 * @todo This class and LircRemoteControlPlugin.Button could be generalized to all
 *   remote controls.
 *
 * @author Stephen Blackheath
 */
public interface LircRemoteControlListener
{
  public void connectStatusChanged(LircRemoteControlPlugin plugin, boolean connected);

  public void buttonPressed(LircRemoteControlPlugin plugin, LircRemoteControlPlugin.Button button);
}

