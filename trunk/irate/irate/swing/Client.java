// Copyright 2003 Anthony Jones

package irate.swing;

import irate.common.TrackDatabase;
import irate.common.UpdateListener;
import irate.download.DownloadThread;
import irate.client.PlayListManager;
import irate.client.PlayThread;
import irate.client.PlayerList;
import irate.client.Player;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
//added for UI tweak by Allen Tipper 14.9.03
import javax.swing.event.*;

//import com.l2fprod.gui.plaf.skin.SkinLookAndFeel;
//end add

public class Client extends JFrame {

  public static void main(String[] args) {
    /*try {
	  	
      skin: {
        for (int i = 0; i < args.length - 1; i++) {
          if (args[i].equals("--skin")) {
            SkinLookAndFeel.setSkin(SkinLookAndFeel.loadThemePack(args[i + 1]));
            SkinLookAndFeel.enable();
            break skin;
          }
        }
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
      }
    }
    catch (Exception e) {
      try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      } catch (Exception exc) {
        System.err.println("Error loading L&F: " + exc);
      }
    } */
    
    try {
      Client client = new Client() {
        public void actionClose() {
          super.actionClose();
          System.exit(0);
        }
      };
      client.show();
      if (client.menuItemAccount.isEnabled())
        client.actionAccount();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private PlayListManager playListManager;
  private TrackDatabase trackDatabase;
  private PlayerList playerList;
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
     playListManager = new PlayListManager(trackDatabase);
    
     playerList = new PlayerList();
     playThread = new PlayThread(playListManager, playerList);
     playPanel = new PlayPanel(playListManager, playThread);
     playThread.start();
     getContentPane().add(playPanel, BorderLayout.CENTER);
     
     errorDialog = new ErrorDialog(this);
     
     downloadThread = new DownloadThread(trackDatabase) {
	     public void process() throws IOException {
		 menuItemDownload.setEnabled(false);
		 super.process();
		 perhapsDisableAccount();
		 menuItemDownload.setEnabled(true);
       }

       public void handleError(String code, String urlString) {
         actionSetContinuousDownload(false);
         URL url;
         if (urlString.indexOf(':') < 0)
           url = getResource("help/" + urlString);
         else 
           try {
             url = new URL(urlString);
           }
           catch (MalformedURLException e) {
             e.printStackTrace();
             url = getResource("help/malformedurl.html");
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
     getContentPane().add(downloadPanel, BorderLayout.SOUTH);

       // Add a close action listener.
     addWindowListener(new WindowAdapter() {
       public void windowClosing(WindowEvent e) {
         actionClose();
       }
     });

     setJMenuBar(createMenuBar());
     downloadThread.start();
   }

   public URL getResource(String s) {
     return playThread.getClass().getResource(s);
   }

   /** Disable the 'Account' settings if the number of tracks is non-zero.
    * The server gets confused if you already have tracks and you try to create 
    * access a different account name. */  
   private void perhapsDisableAccount() {
     menuItemAccount.setEnabled(trackDatabase.getNoOfTracks() == 0);
   }

   public void actionDownload() {
     if (trackDatabase.hasRatedEnoughTracks())
       downloadThread.go();
     else
       errorDialog.showURL(getResource("help/notenoughratings.html"));
   }

   public void actionSetContinuousDownload(boolean state) {
     if (trackDatabase.hasRatedEnoughTracks()) {
       menuItemContinuousDownload.setState(state);
       downloadThread.setContinuous(state);
       downloadThread.go();
     }
     else {
       errorDialog.showURL(getResource("help/notenoughratings.html"));
     }
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
     errorDialog.showURL(getResource("help/gettingstarted.html"));
   }

   public void actionAbout() {
     errorDialog.showURL(getResource("help/about.html"));
   }

   public JMenu createActionMenu() {
     JMenu m = new JMenu("Action");

     menuItemDownload = new JMenuItem("Download");
     menuItemDownload.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         actionDownload();
       }
 	});
    //Added for UI niceness by Allen Tipper 14.9.03
    
    menuItemDownload.addMenuDragMouseListener(new MenuDragMouseListener() {
      public void menuDragMouseDragged(MenuDragMouseEvent e){}
      public void menuDragMouseEntered(MenuDragMouseEvent e){
	downloadThread.setState("Download a new song");
      }
      public void menuDragMouseExited(MenuDragMouseEvent e){
	downloadThread.doCheckAutoDownload();
      }
      public void menuDragMouseReleased(MenuDragMouseEvent e){
	downloadThread.doCheckAutoDownload();
      }
	});
    //end add
    m.add(menuItemDownload);

    menuItemContinuousDownload = new JCheckBoxMenuItem("Continuous download");
    menuItemContinuousDownload.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        actionSetContinuousDownload(menuItemContinuousDownload.getState());
      }
    });
    //Added for UI niceness by Allen Tipper 14.9.03

    menuItemContinuousDownload.addMenuDragMouseListener(new MenuDragMouseListener() {
	    public void menuDragMouseDragged(MenuDragMouseEvent e){}
	    public void menuDragMouseEntered(MenuDragMouseEvent e){
		downloadThread.setState("Continuously download new songs");
	    }
	    public void menuDragMouseExited(MenuDragMouseEvent e){
		downloadThread.doCheckAutoDownload();
	    }
	    public void menuDragMouseReleased(MenuDragMouseEvent e){
		downloadThread.doCheckAutoDownload();
	    }
        });
    //end add
    m.add(menuItemContinuousDownload);

    JMenuItem purge = new JMenuItem("Purge");
    purge.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        trackDatabase.purge();
        playPanel.update();
      }
    });
    //Added for UI niceness by Allen Tipper 14.9.03

    purge.addMenuDragMouseListener(new MenuDragMouseListener() {
	    public void menuDragMouseDragged(MenuDragMouseEvent e){}
	    public void menuDragMouseEntered(MenuDragMouseEvent e){
		downloadThread.setState("Purge 0-rated songs");
	    }
	    public void menuDragMouseExited(MenuDragMouseEvent e){
		downloadThread.doCheckAutoDownload();
	    }
	    public void menuDragMouseReleased(MenuDragMouseEvent e){
		downloadThread.doCheckAutoDownload();
	    }
        });
    //end add
    m.add(purge);
    
    JMenuItem exit = new JMenuItem("Close");
    exit.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        actionClose();
      }
    });
    //Added for UI niceness by Allen Tipper 14.9.03

    exit.addMenuDragMouseListener(new MenuDragMouseListener() {
	    public void menuDragMouseDragged(MenuDragMouseEvent e){}
	    public void menuDragMouseEntered(MenuDragMouseEvent e){
		downloadThread.setState("Close iRate Radio");
	    }
	    public void menuDragMouseExited(MenuDragMouseEvent e){
		downloadThread.doCheckAutoDownload();
	    }
	    public void menuDragMouseReleased(MenuDragMouseEvent e){
		downloadThread.doCheckAutoDownload();
	    }
	});
    //end add
    m.add(exit);
    return m;
  }

  public JMenuItem createPlayer(Player player) {
    final String name = player.getName();
    JCheckBoxMenuItem mi = new JCheckBoxMenuItem(name); 
    mi.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        trackDatabase.setPlayer(name);
      }
    });
    //Added for UI niceness by Allen Tipper 14.9.03

    mi.addMenuDragMouseListener(new MenuDragMouseListener() {
            public void menuDragMouseDragged(MenuDragMouseEvent e){}
            public void menuDragMouseEntered(MenuDragMouseEvent e){
                downloadThread.setState("Set mp3 player to " + name);
            }
            public void menuDragMouseExited(MenuDragMouseEvent e){
                downloadThread.doCheckAutoDownload();
            }
            public void menuDragMouseReleased(MenuDragMouseEvent e){
                downloadThread.doCheckAutoDownload();
            }
        });
    //end add
    playerButtonGroup.add(mi);
    if (name.equals(trackDatabase.getPlayer()))
      mi.setState(true);  
    
    return mi; 
  }

  public JMenu createDownloadMenu() {
    int autoDownload = trackDatabase.getAutoDownload(); 
    
    JMenu m = new JMenu("Auto download");
    //Added by Allen Tipper for UI niceness 14.9.03
    m.addMenuListener(new MenuListener() {
      public void menuSelected(MenuEvent e){
        downloadThread.setState("Set number of unrated tracks to stop automatic downloading");
      }
      public void menuDeselected(MenuEvent e){
	downloadThread.doCheckAutoDownload();
      }
      public void menuCanceled(MenuEvent e){
	downloadThread.doCheckAutoDownload();
      }
    });
    //end add
    ButtonGroup bg = new ButtonGroup();
    int counts[] = new int[] {0, 3, 5, 11};
    for (int i = 0; i < counts.length; i++) {
      final int count = counts[i];
      JCheckBoxMenuItem mi = new JCheckBoxMenuItem(count == 0 ? "Disabled" : "< " + count + " unrated tracks");
      mi.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          trackDatabase.setAutoDownload(count);
          downloadThread.checkAutoDownload();
        }
      });
      //Added for UI niceness by Allen Tipper 14.9.03

      mi.addMenuDragMouseListener(new MenuDragMouseListener() {
	      public void menuDragMouseDragged(MenuDragMouseEvent e){}
	      public void menuDragMouseEntered(MenuDragMouseEvent e){
		  downloadThread.setState("Set to download new tracks automatically when you have less than " + count + " unrated tracks");
	      }
	      public void menuDragMouseExited(MenuDragMouseEvent e){
		  downloadThread.doCheckAutoDownload();
	      }
	      public void menuDragMouseReleased(MenuDragMouseEvent e){
		  downloadThread.doCheckAutoDownload();
	      }
	  });
      //end add
      bg.add(mi);
      mi.setState(count == autoDownload);
      m.add(mi);
    }
    
    return m;
  }

  public JMenu createPlayListMenu() {
    int autoDownload = trackDatabase.getPlayListLength(); 
    
    JMenu m = new JMenu("Play list");
    //Added by Allen Tipper for UI niceness 14.9.03
    m.addMenuListener(new MenuListener() {
      public void menuSelected(MenuEvent e){
      	downloadThread.setState("Set number of tracks in playlist");
      }
      public void menuDeselected(MenuEvent e){
       	downloadThread.doCheckAutoDownload();
      }
      public void menuCanceled(MenuEvent e){
       	downloadThread.doCheckAutoDownload();
      }
    });
    //end add
    ButtonGroup bg = new ButtonGroup();
    int[] counts = new int[] { 5, 7, 13, 19, 31 };
    for (int i = 0; i < counts.length; i++) {
      final int count = counts[i];
      JCheckBoxMenuItem mi = new JCheckBoxMenuItem(count + " tracks");
      mi.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          trackDatabase.setPlayListLength(count);
        }
      });
      //Added for UI niceness by Allen Tipper 14.9.03

      mi.addMenuDragMouseListener(new MenuDragMouseListener() {
              public void menuDragMouseDragged(MenuDragMouseEvent e){}
              public void menuDragMouseEntered(MenuDragMouseEvent e){
                  downloadThread.setState("Set your play list to " + count + " number of tracks");
              }
              public void menuDragMouseExited(MenuDragMouseEvent e){
                  downloadThread.doCheckAutoDownload();
              }
              public void menuDragMouseReleased(MenuDragMouseEvent e){
                  downloadThread.doCheckAutoDownload();
              }
          });
      //end add
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
    //Added for UI niceness by Allen Tipper 14.9.03

    menuItemAccount.addMenuDragMouseListener(new MenuDragMouseListener() {
	    public void menuDragMouseDragged(MenuDragMouseEvent e){}
	    public void menuDragMouseEntered(MenuDragMouseEvent e){
		downloadThread.setState("Account settings");
	    }
	    public void menuDragMouseExited(MenuDragMouseEvent e){
		downloadThread.doCheckAutoDownload();
	    }
	    public void menuDragMouseReleased(MenuDragMouseEvent e){
		downloadThread.doCheckAutoDownload();
	    }
	});
    //end add
    m.add(menuItemAccount);
    perhapsDisableAccount();

    if (playThread.isSpeechSupported()) {
      final JCheckBoxMenuItem roboJock = new JCheckBoxMenuItem("Enable RoboJock");
      roboJock.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          trackDatabase.setRoboJockEnabled(roboJock.getState());
        }
      });
      roboJock.setState(trackDatabase.isRoboJockEnabled());
      m.add(roboJock);
    }
    
    /* 
     * Create player selection menu.
     */
    playerButtonGroup = new ButtonGroup();
    JMenu player = new JMenu("Player");
    Player[] players = playerList.getPlayers();
    if (players.length == 0) {
      JMenuItem none = new JMenuItem("(none)");
      none.setEnabled(false);
      player.add(none);
    }
    else {
      for (int i = 0; i < players.length; i++)
        player.add(createPlayer(players[i]));
    }
    //Added by Allen Tipper for UI niceness 14.9.03
    player.addMenuListener(new MenuListener() {
      public void menuSelected(MenuEvent e){
	downloadThread.setState("Set mp3 player");
      }
      public void menuDeselected(MenuEvent e){
	downloadThread.doCheckAutoDownload();
      }
      public void menuCanceled(MenuEvent e){
	downloadThread.doCheckAutoDownload();
      }
    });
    //end add
    m.add(player);

    m.add(createDownloadMenu());
    m.add(createPlayListMenu());

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
