/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import info.joriki.awt.image.CMYKColorModel;
import info.joriki.awt.image.BufferColorModel;
import info.joriki.awt.image.QuarticCMYKColorModel;

public class DeviceCMYKColorSpace extends DeviceColorSpace
{
  final static float [] black = {0,0,0,1};

  DeviceCMYKColorSpace ()
  {
    super (CMYK,black,
           oldAdobeCMYK.isSet () ?
           (BufferColorModel) QuarticCMYKColorModel.oldAdobeCMYK :
           adobeCMYK.isSet () ?
           (BufferColorModel) QuarticCMYKColorModel.adobeCMYK :
           (BufferColorModel) new CMYKColorModel ());
  }

  float [] toRGBArray (float [] cmyk)
  {
    return ((BufferColorModel) colorModel).getRGB (cmyk);
  }
}
