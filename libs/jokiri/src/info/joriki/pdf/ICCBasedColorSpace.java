/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import java.awt.color.ICC_Profile;
import java.awt.color.ICC_ProfileGray;
import java.awt.color.ICC_ColorSpace;

import info.joriki.io.Resources;
import info.joriki.io.Util;
import info.joriki.util.Assertions;

public class ICCBasedColorSpace extends CIEBasedColorSpace
{
  // The API for ICC_ColorSpace says that values are expressed
  // relative to the D50 white point {.9642,1,.8249}. However,
  // full white comes out approximately 255/256 times these
  // values. As far as I understand results for other gray
  // values, they seem to be off by varying amounts, but never
  // by more than 1/256 in either direction; full black comes
  // out as full black. Since black and white are the only
  // important ones to get exactly right, whereas changes by
  // 1/256 in other colors are tolerable, the following scaling
  // seems like a reasonable solution.
  public final static float [] D50 = {.9642f,1f,.8249f};

  static {
    for (int i = 0;i < 3;i++)
      D50 [i] *= 255/256f;
  }

  final static byte [] sRGB;
  
  static {
    try {
      sRGB = Resources.getBytes (ICCBasedColorSpace.class,"sRGB.icc");
      if (sRGB == null)
        throw new Error ("missing sRGB profile");
    } catch (IOException e) {
      e.printStackTrace();
      throw new Error ("couldn't read sRGB profile");
    }
  }
  
  boolean isSRGB;
  ICC_Profile profile;
  ICC_ColorSpace colorSpace;

  public ICCBasedColorSpace (int ncomponents,InputStream in,float [] range) throws IOException
  {
    super (ncomponents);
    if (range != null) // otherwise keep [0,1] default
      defaultDecode = range;
    byte [] data = Util.undump (in);
    isSRGB = Arrays.equals (data,sRGB);
    profile = ICC_Profile.getInstance (data);
    // otherwise toPixel in CIEBasedColorSpace might be wrong
    Assertions.expect (profile instanceof ICC_ProfileGray,ncomponents == 1);
    colorSpace = new ICC_ColorSpace (profile);
  }

  public boolean isSRGB () {
    return isSRGB;
  }
  
  protected float [] toXYZ (float [] abc)
  {
    float [] xyz = colorSpace.toCIEXYZ (abc);
    // normalize XYZ to white = (1,1,1). See awt.image.CalRGBColorModel,
    // and see the comment for whitePoint above.
    for (int i = 0;i < 3;i++)
      xyz [i] /= D50 [i];
    return xyz;
  }
  
  public ICC_Profile getProfile ()
  {
    return profile;
  }
  
  public int getType () {
    return profile.getColorSpaceType ();
  }
}
