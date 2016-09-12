/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import info.joriki.awt.image.CalibratedColorModel;

public abstract class CalibratedColorSpace extends PDFColorSpace
{
  protected CalibratedColorSpace (int ncomponent,CalibratedColorModel colorModel)
  {
    super (new float [ncomponent],colorModel);
  }

  float [] toRGBArray (float [] rgb)
  {
    return ((CalibratedColorModel) colorModel).getRGB (rgb);
  }
}
