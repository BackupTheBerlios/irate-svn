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
import java.util.zip.ZipInputStream;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

/**
 * @author Anthony Jones
 */
public class SkinManager {

  private Vector items = new Vector();

  private Hashtable itemHash = new Hashtable();

  private TransparencyManager transparencyManager = new TransparencyManager();

  private final String IMAGE_EXTENSION = ".png";

  public SkinManager() {
  }

  public SkinItem add(SkinItem skinItem) {
    items.add(skinItem);
    itemHash.put(skinItem.getName(), skinItem);
    return skinItem;
  }

  public SkinItem add(Skinable button, String name) {
    return add(new SkinItem(button, name));
  }

  public SkinItem addControl(final Control control, String name) {
    control.addPaintListener(new PaintListener() {
      public void paintControl(PaintEvent e) {
        ImageData imageData = transparencyManager.getBackground(control);
        if (imageData == null)
          return;
        Image img = new Image(control.getDisplay(), imageData);
        e.gc.drawImage(img, 0, 0);
        img.dispose();
      }
    });
    
    return add(new Skinable() {
      public void setTransparencyManager(TransparencyManager tm) {
      }

      public void setText(String text) {
      }

      public void setPressedText(String text) {
      }

      public void setToolTipText(String text) {
        control.setToolTipText(text);
      }

      public void setImage(String key, ImageData imageData) {
        if (key.equals(""))
          transparencyManager.associate(control, imageData);
      }

      public void redraw() {
      }
    }, name);
  }

  public SkinItem addItem(final Item item, String name) {
    return add(new Skinable() {

      public void setTransparencyManager(TransparencyManager tm) {
      }

      public void setText(String text) {
        item.setText(text);
      }

      public void setPressedText(String text) {
      }

      public void setToolTipText(String text) {
        if (item instanceof ToolItem)
          ((ToolItem) item).setToolTipText(text);
      }

      public void setImage(String key, ImageData imageData) {
        if (key.equals("")) {
          item.setImage(new Image(item.getDisplay(), imageData));
          item.setText("");
        }
      }

      public void redraw() {
      }
    }, name);
  }

  public void applySkin(InputStream is) {
    for (Iterator itr = items.iterator(); itr.hasNext();) {
      SkinItem skinItem = (SkinItem) itr.next();
      skinItem.pre();
    }

    ZipInputStream zis = new ZipInputStream(is);
    try {
      ZipEntry ze;
      while ((ze = zis.getNextEntry()) != null) {
        String name = ze.getName();
        System.out.println("skin: " + name);
        if (name.endsWith(IMAGE_EXTENSION)) {
          ImageData imageData = new ImageData(zis);
          try {
            int index = name.indexOf('-');
            String itemName;
            String key;
            if (index >= 0) {
              itemName = name.substring(0, index);
              key = name.substring(index + 1, name.length()
                  - IMAGE_EXTENSION.length());
            }
            else {
              itemName = name.substring(0, name.length()
                  - IMAGE_EXTENSION.length());
              key = "";
            }
            SkinItem skinItem = (SkinItem) itemHash.get(itemName);
            if (skinItem != null)
              skinItem.setImage(key, imageData);
          } catch (Exception e) {
            e.printStackTrace();
          }
        } 
        else {
          zis.closeEntry();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    for (Iterator itr = items.iterator(); itr.hasNext();) {
      SkinItem skinItem = (SkinItem) itr.next();
      skinItem.post();
    }
  }

  public class SkinItem {

    private Skinable skinable;

    private String name;

    private boolean gotImage;

    public SkinItem(Skinable button, String name) {
      button.setTransparencyManager(transparencyManager);
      this.skinable = button;
      this.name = name;
      pre();
      post();
    }

    public String getName() {
      return name;
    }

    public void pre() {
      skinable.setText(Resources.getString(name));
      skinable.setToolTipText(Resources.getString(name + ".tooltip"));
      gotImage = false;
    }

    public void setImage(String key, ImageData imageData) {
      skinable.setImage(key, imageData);
      gotImage = true;
    }

    public void post() {
      if (!gotImage) System.err.println("No image: " + name);
      skinable.redraw();
    }
  }
}