/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image;

import info.joriki.util.Assertions;

final public class YUVColorModel extends BufferColorModel
{
  public YUVColorModel ()
  {
    super (3,false);
  }

  /*
    The Web is full of conflicting information about YUV / YCbCr.
    However, JPEG.txt on whotsit.org and the libjpeg source agree
    on what the correct transform for JPEG is. They give approximate
    values, but it seems this is how to derive the exact ones:
    Y is determined using the coefficients below, which are exact
    by definition, and Cb and Cr are determined by the requirement that
    Cb is a multiple of (B - Y) with total coefficient .5 for B, and
    Cr is a multiple of (R - Y) with total coefficient .5 for R:
    This implies the following structure for the inverse transform:
    /R\   /1   0 VR\ /Y\
    |G| = |1  UG VG| |U|
    \B/   \1  UB  0/ \V/
    and VR, UG, VG, UB are derived from the requirement that
    this be the inverse transform.
    The resulting RGBtoYUV matrix coincides with the matrix given in
    Eq 3 (item 28) when the latter is scaled as described in the
    lines preceding it.

    Added 31/8/02: It also coincides with the one given in
    http://www.libpng.org/pub/mng/spec/jng.html
    (References\JNG 1_0.htm)
    
    Added 18/1/03: It also coincides with the one given in
    jccolor.c in the jpeg library that comes with ghostscript,
    and the one given in JPEG File Interchange Format, Version 1.02
    (References\jfif.pdf)

    Added 10/8/03: It also coincides with the one given in
    the JPEG 2000 standard
  */

  private final static float [] ycoefs = {.299f,.587f,.114f};
  public  final static float [] [] RGBtoYUV = new float [3] [3];

  public  final static float UB = 2 * (1 - ycoefs [2]); // 1.772
  public  final static float VR = 2 * (1 - ycoefs [0]); // 1.402
  public  final static float UG = -UB * ycoefs [2] / ycoefs [1];
  public  final static float VG = -VR * ycoefs [0] / ycoefs [1];

  static {
    RGBtoYUV [0] = ycoefs;
    for (int i = 0;i < 3;i++)
      {
        RGBtoYUV [1] [i] = -ycoefs [i] / UB;
        RGBtoYUV [2] [i] = -ycoefs [i] / VR;
      }
    RGBtoYUV [1] [2] = .5f;
    RGBtoYUV [2] [0] = .5f;
  }

  final float [] rgb = new float [3];

  public float [] getRGB (float [] yuv)
  {
    float y = yuv [0];
    float u = yuv [1] - .5f;
    float v = yuv [2] - .5f;

    rgb [0] = y          + VR * v;
    rgb [1] = y + UG * u + VG * v;
    rgb [2] = y + UB * u         ;

    return rgb;
  }

  public static void toRGB (float [] ys,float [] us,float [] vs)
  {
    Assertions.expect (us.length,ys.length);
    Assertions.expect (vs.length,ys.length);

    for (int i = 0;i < ys.length;i++)
      {
	float y = ys [i];
	float u = us [i];
	float v = vs [i];
	ys [i] = y          + VR * v;
	us [i] = y + UG * u + VG * v;
	vs [i] = y + UB * u;
      }
  }

  public boolean equals (Object o)
  {
    return o instanceof YUVColorModel;
  }
}
