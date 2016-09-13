/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import info.joriki.awt.image.CalRGBColorModel;

import info.joriki.util.General;

public class CalRGBColorSpace extends CalibratedColorSpace
{
  final static float [] defaultGamma = {1,1,1};
  final static float [] defaultMatrix = {1,0,0,0,1,0,0,0,1};

  public CalRGBColorSpace (PDFDictionary dictionary)
  {
    super (3,new CalRGBColorModel
      (General.toMatrix
       (dictionary.getFloatArray ("Matrix",defaultMatrix)),
       dictionary.getFloatArray ("WhitePoint"),
       dictionary.getFloatArray ("Gamma",defaultGamma),
       false));

    checkBlackPoint (dictionary);
    dictionary.checkUnused ("4.14");
  }
}
