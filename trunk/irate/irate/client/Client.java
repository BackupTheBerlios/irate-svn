package irate.client;

import irate.common.TrackDatabase;
import irate.download.DownloadThread;
import irate.download.DownloadPanel;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class Client extends JFrame {

  public static void main(String[] args) {
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
  private JMenuItem menuItemAccount;
  private ErrorDialog errorDialog;
 
  public Client() throws Exception {
    setTitle("iRATE radio");

    setSize(600, 400);
    setJMenuBar(createMenuBar());
  
    File file = new File("trackdatabase.xml");
    try {
      trackDatabase = new TrackDatabase(file);
      perhapsDisableAccount();
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
    downloadThread.addActionListener(new ActionListener() {
      private String state = "";
      public void actionPerformed(ActionEvent e) {
        String state = downloadThread.getState();
        if (!state.equals(this.state)) {
          this.state = state;
          playPanel.update();
        }
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

  public JMenu createActionMenu() {
    JMenu m = new JMenu("Action");

    menuItemDownload = new JMenuItem("Download");
    menuItemDownload.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        actionDownload();
      }
    });
    m.add(menuItemDownload);
    
    JMenuItem exit = new JMenuItem("Close");
    exit.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        actionClose();
      }
    });
    m.add(exit);
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
    JMenuItem about = new JMenuItem("About");
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
