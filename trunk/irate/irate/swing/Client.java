package irate.swing;

import irate.common.TrackDatabase;
import irate.common.UpdateListener;
import irate.download.DownloadThread;
import irate.client.PlayListManager;
import irate.client.PlayThread;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class Client extends JFrame {

  public static void main(String[] args) {
    try {
    UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
    }
    catch (Exception e) {
      //e.printStackTrace();
    }
    try {
      Client client = new Client() {
        public void actionClose() {
          super.actionClose();
          System.exit(0);
        }
      };
      client.show();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private PlayListManager playListManager;
  private TrackDatabase trackDatabase;
  private PlayThread playThread;
  private PlayPanel playPanel;
  private DownloadThread downloadThread;
  private DownloadPanel downloadPanel;
  private JMenuItem menuItemDownload;
  private JCheckBoxMenuItem menuItemContinuousDownload;
  private JMenuItem menuItemAccount;
  private ErrorDialog errorDialog;
  private ButtonGroup playerButtonGroup;
 
  public Client() throws Exception {
    setTitle("iRATE radio");

    setSize(640, 400);
  
    File file = new File("trackdatabase.xml");
    try {
      trackDatabase = new TrackDatabase(file);
      trackDatabase.purge();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    playListManager = new PlayListManager(trackDatabase);
    
    playThread = new PlayThread(playListManager);
    playPanel = new PlayPanel(playListManager, playThread);
    playThread.start();
    getContentPane().add(playPanel, BorderLayout.CENTER);

    errorDialog = new ErrorDialog(this);
    
    downloadThread = new DownloadThread(trackDatabase) {
      public void process() {
        menuItemDownload.setEnabled(false);
        super.process();
        perhapsDisableAccount();
        menuItemDownload.setEnabled(true);
      }

      public void handleError(String code, String urlString) {
        actionSetContinuousDownload(false);
        URL url;
        if (urlString.indexOf(':') < 0)
          url = getClass().getResource("help/" + urlString);
        else 
          try {
            url = new URL(urlString);
          }
          catch (MalformedURLException e) {
            e.printStackTrace();
            url = getClass().getResource("help/malformedurl.html");
          }
        errorDialog.showURL(url);
      }
    };
    downloadPanel = new DownloadPanel(downloadThread);
    downloadThread.addUpdateListener(new UpdateListener() {
      private String state = "";
      public void actionPerformed() {
        String state = downloadThread.getState();
        if (!state.equals(this.state)) {
          this.state = state;
          playPanel.update();
        }
      }
    });
    playThread.addUpdateListener(new UpdateListener() {
      public void actionPerformed() {
        downloadThread.checkAutoDownload();
      }
    });
    downloadThread.start();
    getContentPane().add(downloadPanel, BorderLayout.SOUTH);

      // Add a close action listener.
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        actionClose();
      }
    });

    setJMenuBar(createMenuBar());
  }

  /** Disable the 'Account' settings if the number of tracks is non-zero.
   * The server gets confused if you already have tracks and you try to create 
   * access a different account name. */  
  private void perhapsDisableAccount() {
    menuItemAccount.setEnabled(trackDatabase.getNoOfTracks() == 0);
  }

  public void actionDownload() {
    downloadThread.go();
  }

  public void actionClose() {
    setVisible(false);
    try {
      trackDatabase.save();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    playThread.reject();
  }

  public void actionAccount() {
    JDialog accountDialog = new AccountDialog(this, trackDatabase);
    accountDialog.show();
  }

  public void actionGettingStarted() {
    errorDialog.showURL(getClass().getResource("help/gettingstarted.html"));
  }

  public void actionAbout() {
    errorDialog.showURL(getClass().getResource("help/about.html"));
  }

  public void actionSetContinuousDownload(boolean state) {
    menuItemContinuousDownload.setState(state);
    downloadThread.setContinuous(state);
    downloadThread.go();
  }

  public JMenu createActionMenu() {
    JMenu m = new JMenu("Action");

    menuItemDownload = new JMenuItem("Download");
    menuItemDownload.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        actionDownload();
      }
    });
    m.add(menuItemDownload);

    menuItemContinuousDownload = new JCheckBoxMenuItem("Continuous download");
    menuItemContinuousDownload.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        actionSetContinuousDownload(menuItemContinuousDownload.getState());
      }
    });
    m.add(menuItemContinuousDownload);

    JMenuItem purge = new JMenuItem("Purge");
    purge.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        trackDatabase.purge();
        playPanel.update();
      }
    });
    m.add(purge);
    
    JMenuItem exit = new JMenuItem("Close");
    exit.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        actionClose();
      }
    });
    m.add(exit);
    return m;
  }

  public JMenuItem createPlayer(String[] paths, String name) {
    String playerPath;
    if (paths == null) {
      playerPath = "";
    }
    else {
      playerPath = null;
      for (int i = 0; i < paths.length; i++)
        if (new File(paths[i]).exists()) {
          playerPath = paths[i];
          break;
        }
    }

    final String path = playerPath;
    JCheckBoxMenuItem mi = new JCheckBoxMenuItem(name); 
    mi.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        trackDatabase.setPlayer(path);
      }
    });
    mi.setEnabled(playerPath != null);
    playerButtonGroup.add(mi);
    if (path != null && path.equals(trackDatabase.getPlayer()))
      mi.setState(true);  
    
    return mi; 
  }

  public JMenu createDownloadMenu() {
    int autoDownload = trackDatabase.getAutoDownload(); 
    
    JMenu m = new JMenu("Auto download");
    ButtonGroup bg = new ButtonGroup();
    int counts[] = new int[] {0, 5, 11, 17, 23, 29};
    for (int i = 0; i < counts.length; i++) {
      final int count = counts[i];
      JCheckBoxMenuItem mi = new JCheckBoxMenuItem(count == 0 ? "Disabled" : "Every " + count + " plays");
      mi.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          trackDatabase.setAutoDownload(count);
          downloadThread.checkAutoDownload();
        }
      });
      bg.add(mi);
      mi.setState(count == autoDownload);
      m.add(mi);
    }
    
    return m;
  }
    
  public JMenu createSettingsMenu() {
    JMenu m = new JMenu("Settings");
    menuItemAccount = new JMenuItem("Account");
    menuItemAccount.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        actionAccount();
      }
    });
    m.add(menuItemAccount);
    perhapsDisableAccount();

    final JCheckBoxMenuItem roboJock = new JCheckBoxMenuItem("Enable RoboJock");
    roboJock.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        trackDatabase.setRoboJockEnabled(roboJock.getState());
      }
    });
    roboJock.setState(trackDatabase.isRoboJockEnabled());
    roboJock.setEnabled(playThread.isSpeechSupported());
    m.add(roboJock);
    
    playerButtonGroup = new ButtonGroup();
    JMenu player = new JMenu("Player");
    player.add(createPlayer(null, "javalayer"));
    player.add(createPlayer(new String[] { 
        "/usr/bin/mpg123", 
        "/usr/local/bin/mpg123" 
        }, "mpg123"));
    player.add(createPlayer(new String[] { 
        "/usr/bin/madplay", 
        "/usr/local/bin/madplay", 
        "madplay.exe" 
        }, "madplay"));
    m.add(player);

    m.add(createDownloadMenu());

    return m;
  }

  public JMenu createInfoMenu() {
    JMenu m = new JMenu("Info");
    JMenuItem gettingStarted = new JMenuItem("Getting started");
    gettingStarted.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        actionGettingStarted();
      }
    });
    m.add(gettingStarted);
    JMenuItem about = new JMenuItem("Credits");
    about.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        actionAbout();
      }
    });
    m.add(about);
    return m;
  }
  
  public JMenuBar createMenuBar() {
    JMenuBar mb = new JMenuBar();
    mb.add(createActionMenu());
    mb.add(createSettingsMenu());
    mb.add(createInfoMenu());
    return mb;
  }
}
