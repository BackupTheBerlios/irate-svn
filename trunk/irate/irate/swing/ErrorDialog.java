package irate.swing;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ErrorDialog extends JDialog {

  private JEditorPane editorPane;
  
  public ErrorDialog(JFrame owner) {
    super(owner, true);
    editorPane = new JEditorPane();
    editorPane.setEditable(false);
    getContentPane().add(new JScrollPane(editorPane), BorderLayout.CENTER);

    JButton close = new JButton("Close");
    close.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }
    });
    getContentPane().add(close, BorderLayout.SOUTH);

    Dimension size = new Dimension(400, 300);
    setSize(size);

    Dimension os = owner.getSize();
    Point ol = owner.getLocation();
    setLocation(ol.x + (os.width - size.width) / 2, 
        ol.y + (os.height - size.height) / 2);
  }

  public void setURL(URL url) {
    try {
      editorPane.setContentType("text/html");
      editorPane.setPage(url);
    }
    catch (IOException e) {
      e.printStackTrace();
      editorPane.setContentType("text/plain");
      editorPane.setText("Can't find error message: " + url.toString());
    }
  }

  public void showURL(URL url) {
    setURL(url);
    show();
  }
}
