/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image;

public abstract class BufferColorModel extends EfficientColorModel
{
  final float [] buf;
  // the array returned by this method may be overwritten by subsequent calls
  public abstract float [] getRGB (float [] abc);

  protected BufferColorModel (int ncomponents,boolean hasAlpha)
  {
    super (ncomponents,hasAlpha);
    buf = new float [ncomponents];
  }

  protected int getOpaqueRGB (int pixel)
  {
    return ColorConversions.pack
      (getRGB (ColorConversions.unpack (pixel,buf)));
  }
}
