/*
 * Created on Dec 5, 2003
 */
package irate.swt;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

/**
 * @author Anthony Jones
 */
public class SkinManager {
  
  private Vector items = new Vector();
  private Vector controls = new Vector();
  private Vector buttons = new Vector();
  private ZipFile zip;
  private TransparencyManager transparencyManager = new TransparencyManager();
  
  public SkinManager() {
  }
  
  public SkinControl add(Control control, String name) {
    SkinControl skinControl = new SkinControl(control, name);
    add(skinControl);
    return skinControl;
  }
  
  public SkinItem add(Item item, String name) {
    SkinItem skinItem = new SkinItem(item, name);
    add(skinItem);
    return skinItem;
  }
  
  public void add(SkinItem skinItem) {
    items.add(skinItem);
    skinItem.getItem().setText(Resources.getString(skinItem.getName()));
  }
  
  public void add(SkinControl skinControl) {
    controls.add(skinControl);
  }
  
  public void add(ThreeModeButton button, String name) {
   SkinButton skinButton = new SkinButton(button, name);
   buttons.add(skinButton);
  }

  
  public void applySkin(ZipFile zip) {
    this.zip = zip;
    for (Iterator itr = items.iterator(); itr.hasNext(); ) {
      SkinItem skinItem = (SkinItem) itr.next();
      skinItem.update();
    }
    for (Iterator itr = controls.iterator(); itr.hasNext(); ) {
      SkinControl skinControl = (SkinControl) itr.next();
      skinControl.update();
    }
    for (Iterator itr = buttons.iterator(); itr.hasNext(); ) {
      SkinButton skinButton = (SkinButton) itr.next();
      skinButton.update();
    }
  }
  
  public class SkinItem {
  
    private Item item;
    private String name;

    public SkinItem(Item item, String name) {
      this.item = item;
      this.name = name;
    }
  
    public Item getItem() {
      return item;
    }
  
    public String getName() {
      return name;
    }
    
    public void setName(String name) {
      this.name = name;
      update();
    }

    private Image getImage(Display display, String name) throws IOException {
      ZipEntry zipEntry = zip.getEntry(name + ".gif"); // Returns null if not found
      InputStream is = zip.getInputStream(zipEntry);
      ImageData data = new ImageData(is);
      return new Image(display, data);
    }

    public void update() {  
      try {
        Display display = item.getDisplay();
        item.setImage(getImage(display, name));

        if (item instanceof ToolItem)
          try { 
            ((ToolItem) item).setHotImage(getImage(display, name + ".hot"));
          }
          catch (Exception e) {
          }
        
        item.setText("");
        return;
      }
      catch (Exception e) {
      }
      item.setImage(null);
      if (item instanceof ToolItem)
        ((ToolItem) item).setHotImage(null);
      item.setText(Resources.getString(name));
      System.out.println(name);
    }
  }
  
  
  public class SkinControl {
    
    private Control control;
    private String name;
    private boolean associated = false;

    public SkinControl(Control control, String name) {
      this.control = control;
      this.name = name;

      control.addPaintListener(new PaintListener() {
        public void paintControl(PaintEvent e) {
          ImageData imageData = transparencyManager.getBackground(SkinControl.this.control);
          if (imageData == null)
            return;
  
          Image img = new Image(SkinControl.this.control.getDisplay(), imageData);
          e.gc.drawImage(img, 0, 0);
          img.dispose();
        }
      });
    }
  
    public String getName() {
      return name;
    }
    
    public void setName(String name) {
      this.name = name;
      update();
    }

    private ImageData getImageData(String name) throws IOException {
      ZipEntry zipEntry = zip.getEntry(name + ".png"); // Returns null if not found
      if(zipEntry != null) {
        InputStream is = zip.getInputStream(zipEntry);
        return new ImageData(is);
      }
      return null;
    }

    public void update() {
      try {
         transparencyManager.associate(control, getImageData(name));
         control.redraw();
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    } 
  }
  
  public class SkinButton {
    
    private ISkinableButton button;
    private String name;

    public SkinButton(ISkinableButton button, String name) {

      button.setTransparencyManager(transparencyManager);
      this.button = button;
      this.name = name;
    }
    
    public String getName() {
      return name;
    }
    
    public void setName(String name) {
      this.name = name;
      update();
    }

    private ImageData getImageData(String name) throws IOException {
      ZipEntry zipEntry = zip.getEntry(name + ".png"); // Returns null if not found
      if(zipEntry != null) {
        InputStream is = zip.getInputStream(zipEntry);
        return new ImageData(is);
      }
      return null;
    }

    public void update() {
      try {
        button.setNormalText(Resources.getString(name + ".normalText"));
        button.setPressedText(Resources.getString(name + ".pressedText"));
        ImageData normalImg = getImageData(name + ".normal");
        ImageData pressedImg = getImageData(name + ".pressed");
        //button.setNormalImage(normalImg);
        //button.setPressedImage(pressedImg);
        button.redraw();
      } catch (IOException e) { }
  }
  }
}
  
