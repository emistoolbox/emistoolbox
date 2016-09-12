/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image;

import java.io.InputStream;

import java.awt.Rectangle;
import java.awt.image.ColorModel;
import java.awt.image.ImageProducer;

import info.joriki.io.ReadableByteArray;
import info.joriki.io.ReadableInputStream;

import info.joriki.util.Assertions;

public class ImageBuffer extends ImageEncoder
{
  protected Rectangle originalImage;
  protected Object pixels;
  int xOffset;
  int yOffset;

  public void setOffsets (int xOffset,int yOffset) {
    this.xOffset = xOffset;
    this.yOffset = yOffset;
  }

  public void setDimensions (int width,int height) {
    if (originalImage == null)
      super.setDimensions (width,height);
    originalImage = new Rectangle (xOffset,yOffset,width,height);
    Assertions.expect (new Rectangle (this.width,this.height).contains (originalImage));
  }

  protected boolean isNativeColorModel (ColorModel colorModel,boolean withPalette) {
    return true;
  }
  
  protected boolean isAllowedColorModel (ColorModel colorModel,boolean withPalette) {
    return true;
  }
  
  protected void encodePixels (int x,int y,int w,int h,Object pixelBlock,int off,int scan)
  {
    if (pixels == null)
      // TODO: remove Object casts after upgrade to JDK 1.6
      pixels = pixelBlock instanceof byte [] ? (Object) new byte [width * height] : (Object) new int [width * height];
    for (int dy = 0,index = (y + yOffset) * width + x + xOffset;dy < h;dy++,off += scan,index += width)
      System.arraycopy (pixelBlock,off,pixels,index,w);
  }

  public ImageProducer getImageProducer ()
  {
    return new AbstractImageProducer () {
        protected void produceImage ()
        { 
          setParameters (width,height,colorModel,hints,properties);
          for (int i = 0;i < consumers.length;i++)
            if (pixels instanceof byte [])
              consumers [i].setPixels (0,0,width,height,colorModel,
                                       (byte []) pixels,0,width);
            else
              consumers [i].setPixels (0,0,width,height,colorModel,
                                       (int []) pixels,0,width);
          staticImageDone ();
        }
      };
  }

  public InputStream getInputStream ()
  {
    return pixels instanceof byte [] ?
      (InputStream) new ReadableByteArray ((byte []) pixels) :     
      (InputStream) new ReadableInputStream () {
          final int maxShift = colorModel.getPixelSize ();
          int n = -1;
          int shift = 0;
          int pixel;
          int [] intPixels = (int []) pixels;
          public int read ()
          {
            if (shift == 0)
              {
                if (++n >= intPixels.length)
                  return -1;
                pixel = intPixels [n];
                shift = maxShift;
              }

            return (pixel >> (shift -= 8)) & 0xff;
          }
        };
  }

  public int [] getRGBPixels ()
  {
    if (colorModel instanceof RGBColorModel && ((RGBColorModel) colorModel).hasAlpha)
      return getPixels ();

    int [] rgbPixels = new int [width * height];
    for (int i = 0;i < rgbPixels.length;i++)
      rgbPixels [i] = colorModel.getRGB (pixels instanceof byte [] ? ((byte []) pixels) [i] & 0xff : ((int []) pixels) [i]);
    return rgbPixels;
  }

  public byte [] getBytes ()
  {
    return (byte []) pixels;
  }

  public int [] getPixels ()
  {
    return (int []) pixels;
  }
  
  public int getWidth () {
    return width;
  }

  public int getHeight () {
    return height;
  }
  
  public ColorModel getColorModel () {
    return colorModel;
  }
}
