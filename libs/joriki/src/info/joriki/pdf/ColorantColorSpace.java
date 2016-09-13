/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

// This class handles both Separation and DeviceN color spaces
// The minor differences between the two are handled in PDFColorSpace.getInstance
// The All separation color space has its own class.

package info.joriki.pdf;

import java.awt.image.ColorModel;

import java.util.Arrays;

import info.joriki.util.Assertions;

public class ColorantColorSpace extends DerivedColorSpace
{
  PDFArray colorants;
  PDFFunction tintTransform;
  
  final private static float [] darkest (int n)
  {
    float [] darkest = new float [n];
    Arrays.fill (darkest,1);
    return darkest;
  }

  ColorantColorSpace (PDFArray colorants,PDFColorSpace base,PDFFunction tintTransform)
  {
    super (base,darkest (tintTransform.m));
    this.tintTransform = tintTransform;
    this.colorants = colorants;
    Assertions.expect (tintTransform.n,base.ncomponents);
  }

  ColorModel getColorModel ()
  {
    return baseColorSpace.mappedBase.getColorModel ();
  }
    
  int toPixel (float [] tint)
  {
    return baseColorSpace.mappedBase.toPixel (getBaseColor (tint));
  }

  public float [] getBaseColor (float [] tint) {
    return tintTransform.f (tint);
  }
}
