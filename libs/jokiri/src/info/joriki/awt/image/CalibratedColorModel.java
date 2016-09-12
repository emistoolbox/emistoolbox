/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image;

public abstract class CalibratedColorModel extends BufferColorModel
{
  // the array returned by this method may be overwritten by subsequent calls
  abstract protected float [] ABCtoXYZ (float [] abc);
  
  protected CalibratedColorModel (int ncomponents,boolean hasAlpha)
  {
    super (ncomponents,hasAlpha);
  }

  public float [] getRGB (float [] abc)
  {
    return SRGBColorModel.sRGB.XYZtoRGB (ABCtoXYZ (abc));
  }
}
