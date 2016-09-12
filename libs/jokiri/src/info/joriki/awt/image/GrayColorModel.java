/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image;

public class GrayColorModel extends EfficientColorModel
{
  public GrayColorModel (boolean hasAlpha)
  {
    super (1,hasAlpha);
    if (hasAlpha)
      throw new info.joriki.util.NotTestedException ();
  }

  public int getRed   (int pixel) { return pixel & 0xff; }
  public int getGreen (int pixel) { return pixel & 0xff; }
  public int getBlue  (int pixel) { return pixel & 0xff; }

  public int getOpaqueRGB (int pixel)
  {
    return (pixel & 0xff) * 0x10101;
  }

  public boolean equals (Object o)
  {
    return o instanceof GrayColorModel;
  }
}
