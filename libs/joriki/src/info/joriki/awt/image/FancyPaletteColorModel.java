/*
 * Copyright 2006 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image;

import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;

import info.joriki.util.General;
import info.joriki.util.NotTestedException;
import info.joriki.util.NotImplementedException;

public class FancyPaletteColorModel extends ColorModel implements PaletteColorModel
{
  ColorModel base;
  int [] rgbs;
  int [] cmap;

  public FancyPaletteColorModel (ColorModel base,int [] cmap) {
    super (General.indexLength (cmap.length));
    this.base = base;
    this.cmap = cmap;
    rgbs = new int [cmap.length];
    for (int i = 0;i < rgbs.length;i++)
      rgbs [i] = base.getRGB (cmap [i]);
    if (cmap.length > 256)
      throw new NotTestedException ();
    if (base instanceof PaletteColorModel)
      // we could implement this by modifying getBaseColorModel and getPalette
      // such that they loop until they find a non-index model and map
      throw new NotImplementedException ("nested index color models");
    else if (base instanceof IndexColorModel)
      throw new IllegalArgumentException ("use SimplePaletteColorModel instead of IndexColorModel");
  }

  public int getTransparency () {
    int transparency = OPAQUE;
    for (int i = 0;i < rgbs.length;i++) {
      int alpha = getAlpha (i);
      if (alpha == 0)
        transparency = BITMASK;
      else if (alpha != 0xff)
        return TRANSLUCENT;
    }
    return transparency;
  }

  public int getRGB (int pixel) {
    return rgbs [pixel];
  }

  public int getAlpha (int pixel) {
    return getByte (pixel,24);
  }

  public int getRed (int pixel) {
    return getByte (pixel,16);
  }

  public int getGreen (int pixel) {
    return getByte (pixel,8);
  }

  public int getBlue (int pixel) {
    return getByte (pixel,0);
  }

  private int getByte (int pixel,int shift) {
    return (rgbs [pixel] >>> shift) & 0xff;
  }

  public int [] getPalette (boolean rgb) {
    return rgb ? rgbs : cmap;
  }
  
  public int getPaletteSize () {
    return cmap.length;
  }

  public ColorModel getBaseColorModel () {
    return base;
  }

  public void transformToRGB () {
    base = new RGBColorModel (getTransparency () != OPAQUE);
    cmap = rgbs;
  }
}
