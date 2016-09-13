/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.awt.image.ColorModel;

abstract class DeviceColorSpace extends PDFColorSpace
{
  int which;

  protected DeviceColorSpace (int which,float [] defaultColor,ColorModel colorModel)
  {
    super (defaultColor,colorModel);
    this.which = which;
  }
}
