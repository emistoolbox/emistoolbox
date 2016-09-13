/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image;

public class CalGrayColorModel extends CalibratedColorModel
{
  public float [] whitePoint;
  public float gamma;

  final float [] xyz = new float [3];

  public CalGrayColorModel (float gamma,boolean hasAlpha)
  {
    super (1,hasAlpha);
    this.gamma = gamma;
    if (hasAlpha)
      throw new info.joriki.util.NotTestedException ();
  }

  protected float [] ABCtoXYZ (float [] abc)
  {
    // According to the spec, these should be multiplied by the white point.
    // However, this cancels with the XYZ normalization to white = (1,1,1).
    // (See awt.image.CalRGBColorModel)
    xyz [0] = xyz [1] = xyz [2] = (float) Math.pow (abc [0],gamma);
    return xyz;
  }

  public boolean equals (Object o)
  {
    return o instanceof CalGrayColorModel &&
      ((CalGrayColorModel) o).gamma == gamma;
  }
}
