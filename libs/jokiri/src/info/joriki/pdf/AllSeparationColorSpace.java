/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import info.joriki.awt.image.GrayColorModel;

import info.joriki.util.Assertions;

class AllSeparationColorSpace extends PDFColorSpace
{
  public AllSeparationColorSpace ()
  {
    super (new float [] {1},new GrayColorModel (false));
  }

  int toPixel (float [] tint)
  {
    Assertions.expect (tint.length,1);
    Assertions.limit (tint [0],0,1);
    return (int) ((1 - tint [0]) * 255 + .5f);
  }

  float [] toRGBArray (float [] tint)
  {
    Assertions.expect (tint.length,1);
    Assertions.limit (tint [0],0,1);
    float gray = 1 - tint [0];
    return new float [] {gray,gray,gray};
  }
}
