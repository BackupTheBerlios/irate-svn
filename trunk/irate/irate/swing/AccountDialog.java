package irate.swing;

import irate.common.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class AccountDialog extends JDialog {
  
  private TrackDatabase trackDatabase;
  private JPanel inputPanel;
  private JTextField userName;
  private JTextField password;
  private JTextField host;
  private JTextField port;
  
  public AccountDialog(JFrame owner, TrackDatabase trackDatabase) {
    super(owner, "Account settings", true);
    this.trackDatabase = trackDatabase;

    getContentPane().add(createInputPanel(), BorderLayout.NORTH);
    getContentPane().add(createButtonPanel(), BorderLayout.SOUTH);

    pack();
    Dimension size = getSize();
//    size.width = size.width * 3 / 2;
//    size.height = size.height * 3 / 2;
//    setSize(size);
    
    Dimension os = owner.getSize();
    Point ol = owner.getLocation();
    setLocation(ol.x + (os.width - size.width) / 2, 
        ol.y + (os.height - size.height) / 2);
  }

  private JTextField addInput(String title, String text) {
    GridBagConstraints gbc = new GridBagConstraints();
  
    gbc.anchor = GridBagConstraints.WEST;
    gbc.weightx = 0;
    gbc.gridwidth = 1;
    inputPanel.add(new JLabel(title + " "), gbc);
    
    gbc.weightx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    JTextField textField = new JTextField(text, 30);
    inputPanel.add(textField, gbc);

    return textField;
  }

  public JPanel createInputPanel() {
    inputPanel = new JPanel(new GridBagLayout());

    userName = addInput("User name", trackDatabase.getUserName());
    password = addInput("Password", trackDatabase.getPassword());
    host = addInput("Host", trackDatabase.getHost());
    port = addInput("Port", Integer.toString(trackDatabase.getPort()));
    
    return inputPanel;
  }
  
  public void actionAccept() {
    try {
      trackDatabase.setUserName(userName.getText());
      trackDatabase.setPassword(password.getText());
      trackDatabase.setHost(host.getText());
      trackDatabase.setPort(Integer.parseInt(port.getText()));
      setVisible(false);
    }
    catch (NumberFormatException e) {
      e.printStackTrace();
    }
  }

  public void actionCancel() {
    setVisible(false);
  }

  public JPanel createButtonPanel() {
    JPanel p = new JPanel(new GridLayout(1, 0));

    JButton cancel = new JButton("Cancel");
    cancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        actionCancel();
      }
    });
    p.add(cancel);
    
    JButton accept = new JButton("OK");
    accept.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        actionAccept();
      }
    });
    p.add(accept);
    
    return p;
  }
}
