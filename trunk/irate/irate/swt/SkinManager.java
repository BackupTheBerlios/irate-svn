/*
 * Created on Dec 5, 2003
 */
package irate.swt;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;

/**
 * @author Anthony Jones
 */
public class SkinManager {
  
  private Vector items = new Vector();
  private Vector controls = new Vector();
  private ZipFile zip;
  
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

    public SkinControl(Control control, String name) {
      this.control = control;
      this.name = name;
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
      if(zipEntry != null) {
        InputStream is = zip.getInputStream(zipEntry);
        ImageData data = new ImageData(is);
        return new Image(display, data);
      }
      return null;
    }

    public void update() {  
        control.addPaintListener(new PaintListener(){
          public void paintControl(PaintEvent e){
            try {
            Rectangle clientArea = control.getBounds();
            Image img = getImage(control.getDisplay(), name);
            
            if (img == null)
            {
              return;
            }
            
            ImageData data = img.getImageData();
            img.dispose();
            data = data.scaledTo(clientArea.width, clientArea.height);
            img = new Image(control.getDisplay(), data);
            e.gc.drawImage(img, 0, 0);
            img.dispose();
            }
            catch (IOException f) {}
          }
        });
        control.redraw();
  } 
}
}
