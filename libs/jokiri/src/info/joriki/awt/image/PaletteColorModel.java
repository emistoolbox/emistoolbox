/*
 * Copyright 2006 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image;

import java.awt.image.ColorModel;

public interface PaletteColorModel {
  int [] getPalette (boolean rgb);
  int getPaletteSize ();
  ColorModel getBaseColorModel ();
}
