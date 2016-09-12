/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image;

import java.io.IOException;

import java.awt.color.ICC_Profile;
import java.awt.color.ICC_ColorSpace;

import info.joriki.util.Assertions;

public class CMYKColorModel extends BufferColorModel implements ImageOptions
{
  ICC_ColorSpace colorSpace;
  final float [] inv = new float [4];
  final float [] rgb = new float [3];

  boolean invert;

  public CMYKColorModel ()
  {
    this (false);
  }

  public CMYKColorModel (boolean invert)
  {
    super (4,false);

    this.invert = invert;
    
    if (CMYKprofile.isSet ())
      try {
        ICC_Profile profile = ICC_Profile.getInstance (CMYKprofile.get ());
        Assertions.expect (profile.getNumComponents (),4);
        Assertions.expect (profile.getColorSpaceType (),ICC_ColorSpace.TYPE_CMYK);
        colorSpace = new ICC_ColorSpace (profile);
      } catch (IOException ioe) {
        ioe.printStackTrace ();
      }
  }

  public float [] getRGB (float [] cmyk)
  {
    if (invert)
    {
      for (int i = 0;i < inv.length;i++)
        inv [i] = 1 - cmyk [i];
      cmyk = inv;
    }

    if (colorSpace != null)
      return colorSpace.toRGB (cmyk);
    
    float k = cmyk [3];

    // The formula from the PDF spec
    // ghostscript (in gdevphex.c) offers the alternative
    // rgb [i] = (1 - cmyk [i]) * (1 - k);
    for (int i = 0;i < 3;i++)
      rgb [i] = 1 - Math.min (1,cmyk [i] + k);

    return rgb;
  }

  public boolean equals (Object o)
  {
    return o instanceof CMYKColorModel &&
      ((CMYKColorModel) o).invert == invert;
  }
}
