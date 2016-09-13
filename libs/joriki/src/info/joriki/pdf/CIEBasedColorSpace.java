/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.awt.image.ColorModel;

import info.joriki.awt.image.SGrayColorModel;
import info.joriki.awt.image.SRGBColorModel;

/* Only Lab and ICC-based color spaces derive from this.
   The former always have three components, but the latter
   can have any number, including one. We assert in
   ICCBasedColorSpace that one-component ICC-based color
   spaces are actually gray spaces, so we can use the
   sGRAY color model for them. In truncating pixel values
   to bytes, we would like to assert
   (pixel & 0xff) * 0x10101 == pixel
   to make sure, but we can't because of rounding errors. */

abstract public class CIEBasedColorSpace extends PDFColorSpace
{
  protected CIEBasedColorSpace (int ncomponents)
  {
    super (new float [ncomponents],
	   ncomponents == 1 ?
	   (ColorModel) SGrayColorModel.sGRAY :
	   (ColorModel) SRGBColorModel.sRGB);
  }

  int toPixel (float [] abc)
  {
    return toRGB (abc); // automatically gets truncated to a byte if necessary
  }

  float [] toRGBArray (float [] abc)
  {
    return SRGBColorModel.sRGB.XYZtoRGB (toXYZ (abc));
  }

  abstract protected float [] toXYZ (float [] abc);
}
