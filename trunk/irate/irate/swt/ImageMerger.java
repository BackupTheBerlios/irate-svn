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
    ImageData destData = new ImageData(backgroundData.width, backgroundData.height,
          foregroundData.depth, foregroundData.palette,
          backgroundData.scanlinePad, backgroundData.data);
    
    // Merge the foreground onto the background
    int[] foregroundPixels = new int[foregroundData.width];
    int[] backgroundPixels = new int[foregroundData.width];
    int[] destPixels = new int[foregroundData.width];
    for (int i = 0; i < foregroundData.height; i++) {
        foregroundData.getPixels(0, i, foregroundPixels.length, foregroundPixels, 0);
        backgroundData.getPixels(x, y + i, backgroundPixels.length, backgroundPixels, 0);
        
        for (int j = 0; j < destPixels.length; j++) {
          
          // Grab the alpha value of the pixel we're deal with
          int a = foregroundData.getAlpha(j,i);
         
          // Calculate the merge value
          RGB frgb = foregroundData.palette.getRGB(foregroundPixels[j]);
          RGB brgb = backgroundData.palette.getRGB(backgroundPixels[j]);
          int r = (frgb.red * a + brgb.red * (255 - a)) / 255;
          int g = (frgb.green * a + brgb.green * (255 - a)) / 255;
          int b = (frgb.blue * a + brgb.blue * (255 - a)) / 255;
          RGB drgb = new RGB(r, g, b);
          destPixels[j] = destData.palette.getPixel(drgb);
        }
        destData.setPixels(x, y + i, destPixels.length, destPixels, 0);
    }    
    return destData;
  }
}
