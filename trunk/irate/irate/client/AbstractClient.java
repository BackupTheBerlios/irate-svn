/*
 * Created on Sep 29, 2003
 */
package irate.client;

import java.io.File;
import java.io.IOException;

import irate.common.TrackDatabase;
import irate.common.UpdateListener;
import irate.download.DownloadThread;
import irate.plugin.PluginApplication;
import irate.plugin.PluginManager;

/**
 * @author Anthony Jones
 */
public abstract class AbstractClient implements UpdateListener, PluginApplication {
  
  protected TrackDatabase trackDatabase;
  protected PlayListManager playListManager;
  protected PlayerList playerList;
  protected PlayThread playThread;
  protected DownloadThread downloadThread;
  protected PluginManager pluginManager;
  
  public AbstractClient() {
    File home = new File(System.getProperties().getProperty("user.home"));

    // Check the current directory for an existing trackdatabase.xml for
    // compatibility reasons only.
    File dir = new File(".");
    File file = new File(dir, "trackdatabase.xml");
    if (!file.exists()) {
      dir = new File("/irate");
      file = new File(dir, "trackdatabase.xml");
      if (!file.exists()) {
        dir = new File(home, "irate");
        if (!dir.exists())
          dir.mkdir();
        file = new File(dir, "trackdatabase.xml");
      }
    }

    try {
      trackDatabase = new TrackDatabase(file);
    }
    catch (IOException e) {
      e.printStackTrace();
    }

    playerList = new PlayerList();
    playListManager = new PlayListManager(trackDatabase);
    playThread = new PlayThread(playListManager, playerList);

    pluginManager = new PluginManager(this, dir);

    if (playerList.getPlayers().length == 0)
      handleError(null, "help/missingplayer.html");

    playThread.addUpdateListener(this);
    playThread.start();

    downloadThread = new DownloadThread(trackDatabase) {
      public void process() throws IOException {
        super.process();
        // perhapsDisableAccount();
      }

      public void handleError(String code, String urlString) {
        AbstractClient.this.handleError(code, urlString);
      }
    };

    downloadThread.addUpdateListener(new UpdateListener() {
      boolean newState = false;
      public void actionPerformed() {
        setState(downloadThread.getState());
        updateTrackTable();     
      }
    });
  }
  
  public abstract void handleError(String code, String urlString);
  public abstract void setState(String state);
  public abstract void updateTrackTable();

}
