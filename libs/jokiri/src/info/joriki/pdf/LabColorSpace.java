/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.awt.color.ColorSpace;

import info.joriki.util.General;
import info.joriki.util.Assertions;

public class LabColorSpace extends CIEBasedColorSpace
{
  // (L,a,b) = (0,0,0) yields (X,Y,Z) = (R,G,B) = (0,0,0)
  // clipping to the range for a,b is done in the color model.

  final static float [] defaultRange = {-100,100,-100,100};
  final float [] xyz = new float [3];
  float [] whitePoint;

  public LabColorSpace (PDFDictionary dictionary)
  {
    super (3);

    float [] range = dictionary.getFloatArray ("Range",defaultRange);

    defaultDecode [0] = 0;
    defaultDecode [1] = 100;
    System.arraycopy (range,0,defaultDecode,2,4);

    whitePoint = dictionary.getFloatArray ("WhitePoint");
    Assertions.expect (whitePoint [1],1);
    
    checkBlackPoint (dictionary);

    dictionary.checkUnused ("4.15");
  }

  protected float [] toXYZ (float [] abc)
  {
    for (int i = 0;i < 3;i++)
      abc [i] = General.clip (abc [i],defaultDecode [2*i],defaultDecode [2*i+1]);
    
    xyz [1] = (abc [0] + 16) / 116;
    xyz [0] = xyz [1] + abc [1] / 500;
    xyz [2] = xyz [1] - abc [2] / 200;

    for (int i = 0;i < 3;i++)
      {
        float x = xyz [i];
        // according to the spec this should be multiplied by the white point,
        // but it seems that XYZ values are normalized to white = (1,1,1),
        // which undoes this multiplication. See awt.image.CalRGBColorModel.
        xyz [i] = x > 6/29f ? x * x * x : (108/841f) * (x - 4/29f);
      }

    return xyz;
  }

  public int getType () {
    return ColorSpace.TYPE_Lab;
  }
}
