/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import info.joriki.awt.image.GrayColorModel;

public class DeviceGrayColorSpace extends DeviceColorSpace
{
  final static float [] black = new float [1];

  public DeviceGrayColorSpace ()
  {
    super (GRAY,black,new GrayColorModel (false));
  }

  int toPixel (float [] gray)
  {
    return (int) (gray [0] * 255 + .5f);
  }
  
  float [] toRGBArray (float [] gray)
  {
    return new float [] {gray [0],gray [0],gray [0]};
  }
}
