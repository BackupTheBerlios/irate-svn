/*
 * Created on Apr 13, 2004
 */
package irate.swt;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;

/**
 * @author Anthony Jones
 */
public class ImageMerger {
  
  public ImageMerger() {
  }
  
  public ImageData merge(ImageData backgroundData, Point location, ImageData foregroundData) {
    return merge(backgroundData, location.x, location.y, foregroundData);
  }
  
  public ImageData merge(ImageData backgroundData, int x, int y, ImageData foregroundData) {    
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

  public ImageData createBackground(int width, int height, Color backgroundColour, ImageData foregroundData) {
    ImageData destData = new ImageData(width, height,
        foregroundData.depth, foregroundData.palette);
    RGB rgb = backgroundColour.getRGB();
    int pixel = destData.palette.getPixel(rgb);
    int[] pixels = new int[width];
    for (int i = 0; i < pixels.length; i++)
      pixels[i] = pixel;
    for (int y = 0; y < height; y++)
      destData.setPixels(0, y, pixels.length, pixels, 0);
    return destData;
  }
  
  public ImageData merge(Color backgroundColour, ImageData foregroundData) {
    ImageData background = createBackground(foregroundData.width, foregroundData.height, backgroundColour, foregroundData);
    return merge(background, 0, 0, foregroundData);
  }
  
}
