/*
 * Copyright 2003 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image;

public class QuarticCMYKColorModel extends BufferColorModel
{
  public final static QuarticCMYKColorModel adobeCMYK =
    new QuarticCMYKColorModel (new float [] [] {
//        0 0 x x       1 0 x x       0 1 x x       1 1 x x
      {255,255,255},{  0,174,239},{237,  0,140},{ 46, 48,146}, // x x 0 0
      {255,241,  0},{  0,165, 79},{238, 28, 35},{ 54, 53, 57}, // x x 1 0
      { 34, 30, 31},{  0,  3, 35},{ 20,  0,  0},{  0,  0,  0}, // x x 0 1
      { 24, 25,  0},{  0,  3,  0},{ 22,  0,  0},{  0,  0,  0}  // x x 1 1
    });

  public final static QuarticCMYKColorModel oldAdobeCMYK =
    new QuarticCMYKColorModel (new float [] [] {
      {255,255,255},{  0,155,196},{255,  0,124},{ 21,  3,121}, // x x 0 0
      {255,249,  7},{  0,131, 57},{255,  5,  0},{  0,  0,  0}, // x x 1 0
      {  0,  0,  0},{  0,  0,  0},{  0,  0,  0},{  0,  0,  0}, // x x 0 1
      {  0,  0,  0},{  0,  0,  0},{  0,  0,  0},{  0,  0,  0}  // x x 1 1
    });

  float [] [] corners;
  float [] [] results = new float [8] [3];

  public QuarticCMYKColorModel (float [] [] corners)
  {
    super (4,false);
    this.corners = corners;
    for (int j = 0;j < 16;j++)
      for (int k = 0;k < 3;k++)
        corners [j] [k] /= 255;
  }

  public float [] getRGB (float [] cmyk)
  {
    float [] [] source = corners;
    for (int i = 0,n = 8;i < 4;i++,n >>= 1) {
      float s = cmyk [i];
      float t = 1 - s;
      for (int j = 0,l = 0;j < n;) {
        float [] result = results [j++];
        float [] lower = source [l++];
        float [] upper = source [l++];
        for (int k = 0;k < 3;k++)
          result [k] = s * upper [k] + t * lower [k];
      }
      source = results;
    }
    return results [0];
  }

  public boolean equals (Object object)
  {
    if (!(object instanceof QuarticCMYKColorModel))
      return false;
    float [] [] otherCorners = ((QuarticCMYKColorModel) object).corners;
    for (int j = 0;j < 16;j++)
      for (int k = 0;k < 3;k++)
        if (otherCorners [j] [k] != corners [j] [k])
          return false;
    return true;
  }
}
