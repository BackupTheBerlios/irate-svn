package irate.swing;

import irate.common.Track;
import irate.common.UpdateListener;
import irate.download.DownloadThread;

import java.awt.*;
import javax.swing.*;

public class DownloadPanel extends JPanel {

  private DownloadThread downloadThread;
  private JLabel stateLabel = new JLabel(" ");
  private JProgressBar progressBar = new JProgressBar();
  
  public DownloadPanel(DownloadThread downloadThread) {
    super(new BorderLayout());
    this.downloadThread = downloadThread;

    add(stateLabel, BorderLayout.CENTER);
    add(progressBar, BorderLayout.EAST);

    downloadThread.addUpdateListener(new UpdateListener() {
      public void actionPerformed() {
        update();
      }
      public void newTrackStarted(Track track) { }
    });
  }

  public void update() {
    stateLabel.setText(downloadThread.getState());
    progressBar.setValue(downloadThread.getPercentComplete());
  }
}
