package irate.client;

import irate.common.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class PlayPanel extends JPanel {

  private PlayListManager playListManager;
  private PlayThread playThread;
  private JLabel currentSongLabel = new JLabel(" ");
  private JList list = new JList();
  private Track[] tracks;
  
  public PlayPanel(PlayListManager playListManager, PlayThread playThread) {
    super(new BorderLayout());
    this.playListManager = playListManager;
    this.playThread = playThread;
    
    playThread.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        update();
      }
    });
    
    currentSongLabel.setBackground(Color.black);
    currentSongLabel.setForeground(Color.red);
    currentSongLabel.setFont(new Font("Serif", Font.PLAIN, 16));
    add(currentSongLabel, BorderLayout.NORTH);

    add(new JScrollPane(list), BorderLayout.CENTER);

    add(createButtonPanel(), BorderLayout.SOUTH);
  }

  private JPanel createButtonPanel() {
    JPanel panel = new JPanel(new GridLayout(1, 0));

    JButton veryBadButton = new JButton("This sux");
    veryBadButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setRating(0);
      }
    });
    panel.add(veryBadButton);

    JButton badButton = new JButton("Yawn");
    badButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setRating(2);
      }
    });
    panel.add(badButton);
    
    JButton normalButton = new JButton("Not bad");
    normalButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setRating(5);
      }
    });
    panel.add(normalButton);
   
    JButton goodButton = new JButton("Cool");
    goodButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setRating(7);
      }
    });
    panel.add(goodButton);
    
    JButton veryGoodButton = new JButton("Love it");
    veryGoodButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setRating(10);
      }
    });
    panel.add(veryGoodButton);

    JButton skipButton = new JButton(">>");
    skipButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        playThread.reject();
      }
    });
    panel.add(skipButton);
    return panel;
  }

  public void setRating(int rating) {
    int index = list.getSelectedIndex();
    if (index < 0)
      playThread.getCurrentTrack().setRating(rating);
    else
      tracks[index].setRating(rating);
    update();
  }

  public void update() {
    synchronized (this) {
      Track currentTrack = playThread.getCurrentTrack();
      currentSongLabel.setText(currentTrack == null ? " " : currentTrack.toString());

      tracks = playListManager.getPlayList().getTracks();
      String items[] = new String[tracks.length];
      for (int i = 0; i < tracks.length; i++) 
        items[i] = tracks[i].toString();
      Object o = list.getSelectedValue();
      list.setListData(items);
      list.setSelectedValue(o, true);
    }
  }
}
