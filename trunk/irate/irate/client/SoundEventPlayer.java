package irate.client;

import java.io.*;

/**
 * Date Created: Feb 6, 2004
 * Date Updated: $Date: 2004/05/31 04:38:42 $
 * @author Creator: Mathieu Mallet
 * @author Updated: $Author: eythian $
 * @version $Revision: 1.2 $ */

public class SoundEventPlayer {
  private Player player;
  private PlayerList playerList;
  private PlayListManager playListManager;
  
  public SoundEventPlayer(PlayListManager playListManager) {
    this.playListManager = playListManager;
    this.playerList = new PlayerList();
  }
  
  public void PlaySoundEvent(File file) throws PlayerException {
    player = playerList.getPlayer(playListManager.getTrackDatabase().getPlayer());

    player.play(file);

    // Without this, RoboJock can't talk because it fights with the
    // Java player over the sound device.
    player.close();
  }
}
