/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image;

import java.util.Arrays;

import info.joriki.util.Assertions;
import info.joriki.util.NotImplementedException;

/*
  The white point used below is the D65 white point.
  There are surprisingly many different values around on
  the Web for this. Usually it is specfied with 4 digits,
  either as {0.3127,0.3290} or as {0.3127,0.3291}. When
  specified with 6 digits, it is given as {0.312713,0.329016}.
  The page http://www.srgb.com/postscript/psdescription.htm
  specifies {0.3127268660,0.3290235126}, coinciding with the
  page http://www.imcotek.com/pdf/CR-A_wp_paper.pdf, which
  gives XYZ = (0.95047,1,1.08883), corresponding to
  {0.312727,0.329023}. The sRGB profile that comes with
  Acrobat specifies {0.3127,0.3290}, as above. However,
  there are two methods to determine the white point
  independently: one is to calculate the chromaticity
  coordinates corresponding to the trimstimulus values
  of CIE XYZ that are consistently given as
  {0.412453,0.357580,0.180423},
  {0.212671,0.715160,0.072169},
  {0.019334,0.119193,0.950227}.
  The result is {0.312731,0.329033}.
  The result {0.312721,0.329030} is obtained by
  calculating the tristimulus values of the CIE spectrum by summing
  it over the spectral sensitivities of the CIE standard observer,
  as published on the CIE web site. 
  It's hard to see how this could fail to be the normative result,
  unless the samples being summed are actually taken from a
  more precise function published elsewhere. However, since
  the differences are merely in the fifth decimal place, we
  use the standard four-digit values, since this is also what
  PNG requires when we write an sRGB chunk.

  XYZ are normalized such that the white point is (1,1,1).
  I'm not sure why this is done, but experiments
  with white points definitely indicate that both
  Acrobat and Ghostscript do it.
*/

public class CalRGBColorModel extends CalibratedColorModel
{
  final static public float defaultGamma = 2.2f;
  final static public float [] standardGamma =
    new float [] {defaultGamma,defaultGamma,defaultGamma};
  final static public float [] standardWhite =
    new float [] {.3127f,.3290f};
  final static public float [] [] standardPrimaries =
    new float [] [] {{.64f,.33f},{.3f,.6f},{.15f,.06f}};

  // 380 nm to 775 nm in 5 nm intervals
  final public static double [] [] standardObserver = {
    {0.0014,0.0022,0.0042,0.0076,0.0143,0.0232,0.0435,0.0776,0.1344,0.2148,
     0.2839,0.3285,0.3483,0.3481,0.3362,0.3187,0.2908,0.2511,0.1954,0.1421,
     0.0956,0.0580,0.0320,0.0147,0.0049,0.0024,0.0093,0.0291,0.0633,0.1096,
     0.1655,0.2257,0.2904,0.3597,0.4334,0.5121,0.5945,0.6784,0.7621,0.8425,
     0.9163,0.9786,1.0263,1.0567,1.0622,1.0456,1.0026,0.9384,0.8544,0.7514,
     0.6424,0.5419,0.4479,0.3608,0.2835,0.2187,0.1649,0.1212,0.0874,0.0636,
     0.0468,0.0329,0.0227,0.0158,0.0114,0.0081,0.0058,0.0041,0.0029,0.0020,
     0.0014,0.0010,0.0007,0.0005,0.0003,0.0002,0.0002,0.0001,0.0001,0.0001},

    {0.0000,0.0001,0.0001,0.0002,0.0004,0.0006,0.0012,0.0022,0.0040,0.0073,
     0.0116,0.0168,0.0230,0.0298,0.0380,0.0480,0.0600,0.0739,0.0910,0.1126,
     0.1390,0.1693,0.2080,0.2586,0.3230,0.4073,0.5030,0.6082,0.7100,0.7932,
     0.8620,0.9149,0.9540,0.9803,0.9950,1.0000,0.9950,0.9786,0.9520,0.9154,
     0.8700,0.8163,0.7570,0.6949,0.6310,0.5668,0.5030,0.4412,0.3810,0.3210,
     0.2650,0.2170,0.1750,0.1382,0.1070,0.0816,0.0610,0.0446,0.0320,0.0232,
     0.0170,0.0119,0.0082,0.0057,0.0041,0.0029,0.0021,0.0015,0.0010,0.0007,
     0.0005,0.0004,0.0002,0.0002,0.0001,0.0001,0.0001,0.0000,0.0000,0.0000},

    {0.0065,0.0105,0.0201,0.0362,0.0679,0.1102,0.2074,0.3713,0.6456,1.0391,
     1.3856,1.6230,1.7471,1.7826,1.7721,1.7441,1.6692,1.5281,1.2876,1.0419,
     0.8130,0.6162,0.4652,0.3533,0.2720,0.2123,0.1582,0.1117,0.0782,0.0573,
     0.0422,0.0298,0.0203,0.0134,0.0087,0.0057,0.0039,0.0027,0.0021,0.0018,
     0.0017,0.0014,0.0011,0.0010,0.0008,0.0006,0.0003,0.0002,0.0002,0.0001,
     0,0,0,0,0,0,0,0,0,0,
     0,0,0,0,0,0,0,0,0,0,
     0,0,0,0,0,0,0,0,0,0}
  };

  // 300 nm to 830 nm in 5 nm intervals
  final public static double [] D65spectrum =
  { 0.0341, 1.6643, 3.2945,11.7652,20.2360,28.6447,37.0535,38.5011,
    39.9488,42.4302,44.9117,45.7750,46.6383,49.3637,52.0891,51.0323,
    49.9755,52.3118,54.6482,68.7015,82.7549,87.1204,91.4860,92.4589,
    93.4318,90.0570,86.6823,95.7736,104.865,110.936,117.008,117.410,
    117.812,116.336,114.861,115.392,115.923,112.367,108.811,109.082,
    109.354,108.578,107.802,106.296,104.790,106.239,107.689,106.047,
    104.405,104.225,104.046,102.023,100.000,98.1671,96.3342,96.0611,
    95.7880,92.2368,88.6856,89.3459,90.0062,89.8026,89.5991,88.6489,
    87.6987,85.4936,83.2886,83.4939,83.6992,81.8630,80.0268,80.1207,
    80.2146,81.2462,82.2778,80.2810,78.2842,74.0027,69.7213,70.6652,
    71.6091,72.9790,74.3490,67.9765,61.6040,65.7448,69.8856,72.4863,
    75.0870,69.3398,63.5927,55.0054,46.4182,56.6118,66.8054,65.0941,
    63.3828,63.8434,64.3040,61.8779,59.4519,55.7054,51.9590,54.6998,
    57.4406,58.8765,60.3125
  };

  public boolean gammaGuessed;
  public boolean chromaticityGuessed;

  public float [] whitePoint;
  public float [] gamma;
  public float [] [] matrix;
  public float [] [] inverse;

  final float [] rgb = new float [3];
  final float [] tmp = new float [3];
  final float [] xyz = new float [3];

  public CalRGBColorModel (float [] [] primaries,float [] white,
                           float gamma,boolean hasAlpha)
  {
    this (primaries,white,gamma == 0 ? null : new float [] {gamma,gamma,gamma},hasAlpha);
  }

  /**
     Construct a CalRGB color model from information about
     the primaries, the white point and the gamma values.
     This constructor can be used in two ways.
     Called with a 3x3 matrix and a 3-component white point,
     it uses these directly as the XYZ values of the primaries
     and the white point, respectively. A CalRGB color model
     constructed in this way will not support XYZ-RGB transforms.
     Called with a 3x2 matrix and a 2-component white point,
     it interprets these as the chromaticity coordinates of
     the primaries and the white point, respectively. A CalRGB
     color model constructed in this way will support XYZ-RGB
     transforms.
  */
  public CalRGBColorModel (float [] [] primaries,float [] white,float [] gamma,boolean hasAlpha)
  {
    super (3,hasAlpha);

    if (gamma == null)
      {
        gamma = standardGamma;
        gammaGuessed = true;
      }
    
    if (white == null)
      {
        white = standardWhite;
        chromaticityGuessed = true;
      }

    if (primaries == null)
      {
        primaries = standardPrimaries;
        chromaticityGuessed = true;
      }

    this.gamma = gamma;

    if (white.length == 3) // XYZ values provided directly
      {
        matrix = primaries;
        whitePoint = white;

        Assertions.expect (whitePoint [1],1);

        for (int i = 0;i < 3;i++)
          {
            float sum = 0;
            for (int j = 0;j < 3;j++)
              sum += matrix [j] [i];
            // the sum is sometimes off by .001, plus we have to allow for rounding
            if (Math.abs (sum - whitePoint [i]) > 1e-3 + 1e-7)
              throw new NotImplementedException ("!(R = G = B = 1)");
          }
      }
    else // chromaticity coordinates provided
      {
        matrix = new float [3] [3];
        inverse = new float [3] [3];
        whitePoint = new float [3];

        for (int i = 0;i < 4;i++)
          {
            float [] src = i == 3 ? white : primaries [i];
            float [] dest = i == 3 ? whitePoint : matrix [i];
            float x = src [0];
            float y = src [1];
            dest [0] = x / y;
            dest [1] = 1;
            dest [2] = (1 - x - y) / y;
          }

        float det = 0;
        for (int i = 0;i < 3;i++)
          {
            int i1 = (i + 1) % 3;
            int i2 = (i + 2) % 3;
            float a1 = matrix [i1] [0];
            float b1 = matrix [i1] [2];
            float a2 = matrix [i2] [0];
            float b2 = matrix [i2] [2];
            inverse [i] [0] = b1 - b2;
            inverse [i] [1] = b2*a1 - b1*a2;
            inverse [i] [2] = a2 - a1;
            det += inverse [i] [1];
          }

        for (int i = 0;i < 3;i++)
          for (int j = 0;j < 3;j++)
            inverse [i] [j] /= det;

        for (int i = 0;i < 3;i++)
          {
            float luminance = 0;
            for (int j = 0;j < 3;j++)
              luminance += inverse [i] [j] * whitePoint [j];
            for (int j = 0;j < 3;j++)
              {
                matrix  [i] [j] *= luminance;
                inverse [i] [j] /= luminance;
              }
          }
      }
  }

  protected static final float clip (float v)
  {
    return v < 0 ? 0 : v > 1 ? 1 : v;
  }

  protected void decode (float [] abc)
  {
    for (int i = 0;i < 3;i++)
      tmp [i] = (float) Math.pow (clip (abc [i]),gamma [i]);
  }

  protected float [] ABCtoXYZ (float [] abc)
  {
    decode (abc);

    for (int j = 0;j < 3;j++) {
      float sum = 0;
      for (int i = 0;i < 3;i++)
        sum += matrix [i] [j] * tmp [i];
      xyz [j] = sum / whitePoint [j];
    }

    return xyz;
  }

  // the array returned by this method may be overwritten by subsequent calls
  public float [] XYZtoRGB (float [] xyz)
  {
    rgb [0] = rgb [1] = rgb [2] = 0;

    for (int i = 0;i < 3;i++) {
      float x = clip (xyz [i]) * whitePoint [i];
      for (int j = 0;j < 3;j++)
        rgb [j] += inverse [j] [i] * x;
    }

    encode (rgb);

    return rgb;
  }

  protected void encode (float [] rgb)
  {
    for (int j = 0;j < 3;j++)
      rgb [j] = (float) Math.pow (clip (rgb [j]),1 / gamma [j]);
  }

  public static float [] getXYZ (double wavelength)
  {
    float [] xyz = new float [3];
    double index = (wavelength - 380e-9) / 5e-9;
    int iindex = (int) index;
    index -= iindex;
    try {
      for (int i = 0;i < 3;i++)
        xyz [i] = (float)
          (standardObserver [i] [iindex] + index * 
           (standardObserver [i] [iindex + 1] -
            standardObserver [i] [iindex]));
    } catch (ArrayIndexOutOfBoundsException aioobe) {}
    return xyz;
  }

  public boolean equals (Object o)
  {
    if (!(o instanceof CalRGBColorModel))
      return false;

    CalRGBColorModel calRGB = (CalRGBColorModel) o;

    for (int i = 0;i < matrix.length;i++)
      if (!Arrays.equals (calRGB.matrix [i],matrix [i]))
        return false;

    return
      Arrays.equals (calRGB.gamma,gamma) &&
      Arrays.equals (calRGB.whitePoint,whitePoint);
  }
}
