/*
 * Created on Apr 13, 2004
 */
package irate.swt;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;

/**
 * @author Anthony Jones
 */
public class ImageMerger {
  
  private TransparencyManager transparencyManager;
  private Control control;
  private Cache cache = new Cache();
  
  public ImageMerger(TransparencyManager transparencyManager, Control control) {
    this.transparencyManager = transparencyManager;
    this.control = control;
  }
  
  public ImageData merge(Point location, ImageData foregroundData) {
    return merge(location.x, location.y, foregroundData);
  }
  
  public ImageData merge(int x, int y, ImageData foregroundData) {
    // Get the background and create a new image which uses the foreground
    // image's palette and depth from the background image. 
    ImageData backgroundData = transparencyManager.getBackground(control);
    if (backgroundData == null)
      return foregroundData;
    
    Merge merge = new Merge();
    merge.x = x;
    merge.y = y;
    merge.foregroundData = foregroundData;
    merge.backgroundData = backgroundData;
    
    ImageData imageData = (ImageData) cache.get(merge);        
    if (imageData == null)
      cache.put(merge, imageData = merge(merge));
    
    return imageData;
  }
  
  private ImageData merge(Merge merge) {        
    ImageData destData = new ImageData(merge.backgroundData.width, merge.backgroundData.height,
          merge.foregroundData.depth, merge.foregroundData.palette,
          merge.backgroundData.scanlinePad, merge.backgroundData.data);
    
    // Merge the foreground onto the background
    int[] foregroundPixels = new int[merge.foregroundData.width];
    int[] backgroundPixels = new int[merge.foregroundData.width];
    int[] destPixels = new int[merge.foregroundData.width];
    for (int i = 0; i < merge.foregroundData.height; i++) {
        merge.foregroundData.getPixels(0, i, foregroundPixels.length, foregroundPixels, 0);
        merge.backgroundData.getPixels(merge.x, merge.y + i, backgroundPixels.length, backgroundPixels, 0);
        
        for (int j = 0; j < destPixels.length; j++) {
          
          // Grab the alpha value of the pixel we're deal with
          int a = merge.foregroundData.getAlpha(j,i);
         
          // Calculate the merge value
          RGB frgb = merge.foregroundData.palette.getRGB(foregroundPixels[j]);
          RGB brgb = merge.backgroundData.palette.getRGB(backgroundPixels[j]);
          int r = (frgb.red * a + brgb.red * (255 - a)) / 255;
          int g = (frgb.green * a + brgb.green * (255 - a)) / 255;
          int b = (frgb.blue * a + brgb.blue * (255 - a)) / 255;
          RGB drgb = new RGB(r, g, b);
          destPixels[j] = destData.palette.getPixel(drgb);
        }
        destData.setPixels(merge.x, merge.y + i, destPixels.length, destPixels, 0);
    }    
    return destData;
  }
  
  private class Merge {
    int x;
    int y;
    ImageData foregroundData;
    ImageData backgroundData;
    
    public boolean equals(Object obj) {
      if (!(obj instanceof Merge))
        return false;
      
      Merge merge = (Merge) obj;
      return x == merge.x && y == merge.y 
          && foregroundData == merge.foregroundData 
          && backgroundData == merge.backgroundData; 
    }
  }
}
