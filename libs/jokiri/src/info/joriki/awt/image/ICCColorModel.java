/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image;

import java.awt.color.ICC_Profile;
import java.awt.color.ICC_ColorSpace;

import java.util.Arrays;

// public constructors take raw data, not profiles
// public methods for getting profile and raw data are provided
// thus we can decide internally whether to cache the data or
// whether to get it from the profile on the fly

public class ICCColorModel extends EfficientColorModel
{
  ICC_ColorSpace colorSpace;
  float [] components;
  boolean invert = true;
  
  public ICCColorModel (byte [] data)
  {
    this (data,false);
  }

  public ICCColorModel (byte [] data,boolean hasAlpha)
  {
    this (data,hasAlpha,false);
  }

  public ICCColorModel (byte [] data,boolean hasAlpha,boolean invert)
  {
    this (ICC_Profile.getInstance (data),hasAlpha,invert);
  }

  private ICCColorModel (ICC_Profile profile,boolean hasAlpha,boolean invert)
  {
    super (profile.getNumComponents (),hasAlpha);
    this.colorSpace = new ICC_ColorSpace (profile);
    this.components = new float [profile.getNumComponents ()];
    this.invert = invert;
  }

  public ICC_Profile getProfile ()
  {
    return colorSpace.getProfile ();
  }

  public byte [] getData ()
  {
    return getProfile ().getData ();
  }

  protected int getOpaqueRGB (int pixel)
  {
    return ColorConversions.pack
      (colorSpace.toRGB
       (ColorConversions.unpack
        (invert ? ~pixel : pixel,components)));
  }

  public boolean equals (Object o)
  {
    return o instanceof ICCColorModel ?
    Arrays.equals (getData (),((ICCColorModel) o).getData ()) :
    false;
  }
}
