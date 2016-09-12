/*
 * Copyright 2003 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image;

public class SRGBColorModel extends CalRGBColorModel
{
  // this rendering intent is currently only used by JPEG2000,
  // for which the spec doesn't specify the rendering intent,
  // and by the PNG decoder if forceSRGB is set.
  public final static SRGBColorModel sRGB = new SRGBColorModel (false,0);
  
  public int renderingIntent;

  public float offset;
  public float cutoff;
  public float linear;

  public SRGBColorModel (boolean hasAlpha,int renderingIntent)
  {
    this (standardPrimaries,standardWhite,2.4f,.055f,hasAlpha,renderingIntent);
  }

  /* The official description of sRGB is full of weird decimal numbers,
     but these can all be calculated from the gamma and the offset.
     The condition is that the linear function that takes over near
     zero be tangent to the offset exponential; this resolves nicely. */
  private SRGBColorModel (float [] [] primaries,float [] white,float gamma,
                           float offset,boolean hasAlpha,int renderingIntent)
  {
    super (primaries,white,gamma,hasAlpha);
    this.renderingIntent = renderingIntent;
    this.offset = offset;

    cutoff = offset / (gamma - 1);
    linear = offset == 0 ? 0 :
      (float) Math.pow ((cutoff + offset) / (1 + offset),gamma) / cutoff;
  }

  protected void decode (float [] abc)
  {
    for (int i = 0;i < 3;i++)
      {
        float clipped = clip (abc [i]);
        tmp [i] =
          clipped < cutoff ?
          clipped * linear :
          (float) Math.pow ((clipped + offset) / (1 + offset),gamma [i]);
      }
  }

  protected void encode (float [] rgb)
  {
    for (int j = 0;j < 3;j++)
      {
        float clipped = clip (rgb [j]);
        rgb [j] =
          clipped < cutoff * linear ?
          clipped / linear :
          (1 + offset) * (float) Math.pow (clipped,1 / gamma [j]) - offset;
      }
  }

  protected int getOpaqueRGB (int pixel)
  {
    return pixel & 0xffffff;
  }

  public boolean equals (Object o)
  {
    return o instanceof SRGBColorModel &&
      ((SRGBColorModel) o).renderingIntent == renderingIntent;
  }
}
