package irate.download;

import java.awt.*;
import java.awt.event.*;
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

    downloadThread.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        update();
      }
    });
  }

  public void update() {
    stateLabel.setText(downloadThread.getState());
    progressBar.setValue(downloadThread.getPercentComplete());
  }
}
