/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import info.joriki.awt.image.RGBColorModel;

public class DeviceRGBColorSpace extends DeviceColorSpace
{
  final static float [] black = new float [3];

  public DeviceRGBColorSpace ()
  {
    super (RGB,black,new RGBColorModel (false));
  }

  float [] toRGBArray (float [] rgb)
  {
    return rgb;
  }
}
