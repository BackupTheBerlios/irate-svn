package irate.client;

import java.io.*;

/**
 * Date Created: Feb 6, 2004
 * Date Updated: $Date: 2004/02/17 21:11:06 $
 * @author Creator: Mathieu Mallet
 * @author Updated: $Author: emh_mark3 $
 * @version $Revision: 1.1 $ */

public class SoundEventPlayer {
  private Thread m_thread = null;
  private boolean bFirstRun = true;
  private Player player;
  private PlayerList playerList;
  private PlayListManager playListManager;
  
  public SoundEventPlayer(PlayListManager playListManager) {
    bFirstRun = true;
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
