/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image;

import info.joriki.util.Assertions;
import info.joriki.util.NotImplementedException;

import java.awt.Transparency;
import java.awt.color.ICC_ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;

abstract public class ImageEncoder extends StaticImageConsumer implements ImageOptions
{
  private Object buffer;
  protected int [] palette;
  protected boolean nativePalette;
  protected ColorModel baseColorModel;

  abstract protected boolean isNativeColorModel (ColorModel colorModel,boolean withPalette);
  
  protected boolean isICCColorModel (ColorModel colorModel,int type) {
    return colorModel instanceof ICCColorModel &&
    ((ICCColorModel) colorModel).getProfile ().getColorSpaceType () == type;
  }
  
  protected boolean isGrayColorModel (ColorModel colorModel) {
    return
    colorModel instanceof GrayColorModel ||
    colorModel instanceof SGrayColorModel ||
    colorModel instanceof CalGrayColorModel ||
    isICCColorModel (colorModel,ICC_ColorSpace.TYPE_GRAY);
  }
  
  protected boolean isRGBColorModel (ColorModel colorModel) {
    return
    colorModel instanceof RGBColorModel ||
    colorModel instanceof SRGBColorModel ||
    colorModel instanceof CalRGBColorModel ||
    colorModel == ColorModel.getRGBdefault () ||
    isICCColorModel (colorModel,ICC_ColorSpace.TYPE_RGB);
  }

  protected boolean isAllowedColorModel (ColorModel colorModel,boolean withPalette) {
    return
    !(noGrayscaleImages.isSet () &&
      isGrayColorModel (colorModel)) &&
    (!standardColorModels.isSet () ||
     colorModel instanceof GrayColorModel ||
     colorModel instanceof SGrayColorModel ||
     colorModel instanceof RGBColorModel ||
     colorModel instanceof SRGBColorModel ||
     colorModel == ColorModel.getRGBdefault ()) &&
    isNativeColorModel (colorModel,withPalette);
  }
  
  public void setColorModel (ColorModel colorModel) {
    super.setColorModel (colorModel);

    if (colorModel instanceof PaletteColorModel) {
      // for a palette color model, we have two "dimensions" of making it allowed if it isn't
      // we can change the color model to default RGB, or we can resolve the palette, or both
      // if either direction on its own would do, we choose the default RGB palette option
      PaletteColorModel paletteColorModel = (PaletteColorModel) colorModel;
      baseColorModel = paletteColorModel.getBaseColorModel ();
      boolean rgb = paletteColorModel.getPaletteSize () > 0x100 ||
        (!isAllowedColorModel (baseColorModel,true) && 
         !isAllowedColorModel (baseColorModel,false));
      palette = paletteColorModel.getPalette (rgb);
      if (rgb)
        baseColorModel = ColorModel.getRGBdefault ();
    }
    else if (isAllowedColorModel (colorModel,false))
      baseColorModel = colorModel;
    else {
      baseColorModel = ColorModel.getRGBdefault ();
      if (colorModel.getPixelSize () <= 8) {
        if (colorModel.getTransparency () != Transparency.OPAQUE)
          throw new NotImplementedException ("grayscale alpha");
        palette = new int [256];
        for (int i = 0;i < palette.length;i++)
          palette [i] = colorModel.getRGB (i);
      }
    }
    nativePalette = palette != null && isAllowedColorModel (baseColorModel,true);
  }
  
  public void setPixels (int x,int y,int w,int h,ColorModel colorModel,byte [] bytes,int off,int scan) {
    usePixels (x,y,w,h,colorModel,bytes,off,scan);
  }
  
  public void setPixels (int x,int y,int w,int h,ColorModel colorModel,int [] ints,int off,int scan) {
    usePixels (x,y,w,h,colorModel,ints,off,scan);
  }
  
  public void usePixels (int x,int y,int w,int h,ColorModel colorModel,Object pixels,int off,int scan) {
    if (colorModel instanceof IndexColorModel && !(colorModel instanceof PaletteColorModel))
      throw new IllegalArgumentException ("use SimplePaletteColorModel instead of IndexColorModel");

    boolean colorModelDiffers = colorModel != this.colorModel;
    if (colorModelDiffers && (nativePalette || baseColorModel != ColorModel.getRGBdefault ())) {
      if (colorModel instanceof PaletteColorModel)
        throw new NotImplementedException ("palette switch");
      throw new Error ("incompatible color models");
    }

    boolean resolvePalette = palette != null && !nativePalette && !colorModelDiffers;
    boolean resolveColors = colorModel != baseColorModel && !nativePalette;

    boolean haveBytes = pixels instanceof byte [];
    boolean needBytes = nativePalette || baseColorModel.getPixelSize () <= 8; 

    if (resolvePalette || resolveColors || haveBytes != needBytes) {
      if (buffer == null || (needBytes ? ((byte []) buffer).length : ((int []) buffer).length) < w)
      // TODO: remove Object casts after upgrade to JDK 1.6
        buffer = needBytes ? (Object) new byte [w] : (Object) new int [w];
      for (int i = 0,offset = off;i < h;i++,offset += scan) {
        for (int j = 0,index = offset;j < w;j++,index++) {
          int b = haveBytes ? ((byte []) pixels) [index] & 0xff : ((int []) pixels) [index];
          b = resolvePalette ? palette [b] : resolveColors ? colorModel.getRGB (b) : b;
          if (needBytes) {
            Assertions.limit (b,0,255);
            ((byte []) buffer) [j] = (byte) b;
          }
          else
            ((int []) buffer) [j] = b;
        }
        encodePixels (x,y + i,w,1,buffer,0,w);
      }
    }
    else
      encodePixels (x,y,w,h,pixels,off,scan);
  }

  protected abstract void encodePixels (int x,int y,int w,int h,Object pixels,int off,int scan);
}
