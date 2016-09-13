/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

abstract public class MarkerColorSpace extends PDFColorSpace
{
  protected MarkerColorSpace (int ncomponent)
  {
    super (new float [ncomponent]); // this "default color" is never used.
  }

  final static String error = "pixel operation on marker color space";
  int toPixel (float [] color) { throw new InternalError (error); }
  float [] toRGBArray (float [] color) { throw new InternalError (error); }
}
