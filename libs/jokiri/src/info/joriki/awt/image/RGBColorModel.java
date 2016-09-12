/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image;

public class RGBColorModel extends EfficientColorModel
{
  public RGBColorModel (boolean hasAlpha)
  {
    super (3,hasAlpha);
  }

  protected int getOpaqueRGB (int pixel)
  {
    return pixel & 0xffffff;
  }

  public boolean equals (Object o)
  {
    return o instanceof RGBColorModel;
  }
}
