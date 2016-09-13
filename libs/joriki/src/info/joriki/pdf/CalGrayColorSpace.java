/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import info.joriki.awt.image.CalGrayColorModel;

import info.joriki.util.Assertions;

public class CalGrayColorSpace extends CalibratedColorSpace
{
  final static float defaultGamma = 1;

  public CalGrayColorSpace (PDFDictionary dictionary)
  {
    super (1,new CalGrayColorModel
      (dictionary.getFloat ("Gamma",defaultGamma),false));

    Assertions.expect (dictionary.getFloatArray ("WhitePoint") [1],1);
    checkBlackPoint (dictionary);
    dictionary.checkUnused ("4.13");
  }
}
