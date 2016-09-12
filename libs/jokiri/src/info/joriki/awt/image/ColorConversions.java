/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image;

import info.joriki.util.Options;

public class ColorConversions
{
  public static float [] XYZ (float [] xy,double sum)
  {
    float [] XYZ = new float [3];
    XYZ [0] = (float) (xy [0] * sum);
    XYZ [1] = (float) (xy [1] * sum);
    XYZ [2] = (float) (sum - XYZ [0] - XYZ [1]);
    return XYZ;
  }

  public static float [] xy (float [] XYZ)
  {
    float [] xy = new float [2];
    double sum = XYZ [0] + XYZ [1] + XYZ [2];
    xy [0] = (float) (XYZ [0] / sum);
    xy [1] = (float) (XYZ [1] / sum);
    return xy;
  }

  final static int max = 0xff;

  public static int quantize (float component)
  {
    if (component < 0)
      {
	Options.warn ("clipping color component");
	return 0;
      }
    if (component > 1)
      {
	Options.warn ("clipping color component");
	return max;
      }
    return (int) (max * component + .5f);
  }

  public static float normalize (int component)
  {
    return component / (float) max;
  }

  public static int pack (float [] components)
  {
    int pixel = 0;
    for (int i = 0;i < components.length;i++)
      pixel = (pixel << 8) | quantize (components [i]);
    return pixel;
  }

  public static float [] unpack (int pixel,float [] components)
  {
    for (int i = components.length - 1;i >= 0;i--)
      {
        components [i] = normalize (pixel & 0xff);
        pixel >>= 8;
      }
    return components;
  }
}
