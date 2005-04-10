package irate.client;

import irate.common.Preferences;
import java.io.*;

/**
 * Date Created: Feb 6, 2004
 * Date Updated: $Date: 2004/09/29 02:09:21 $
 * @author Creator: Mathieu Mallet
 * @author Updated: $Author: lenbok $
 * @version $Revision: 1.3 $ */

public class SoundEventPlayer {
  private Player player;
  private PlayerList playerList;
  private PlayListManager playListManager;
  
  public SoundEventPlayer(PlayListManager playListManager) {
    this.playListManager = playListManager;
    this.playerList = new PlayerList();
  }
  
  public void PlaySoundEvent(File file) throws PlayerException {
    player = playerList.getPlayer(Preferences.getPlayer());

    player.play(file);

    // Without this, RoboJock can't talk because it fights with the
    // Java player over the sound device.
    player.close();
  }
}
