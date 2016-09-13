/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

public class TransparencyGroupAttributes extends GroupAttributes
{
  public PDFObject blendingColorSpace;
  public boolean isolated;
  public boolean knockout;

  TransparencyGroupAttributes (PDFDictionary groupAttributesDictionary)
  {
    blendingColorSpace = groupAttributesDictionary.get ("CS");
    isolated = groupAttributesDictionary.getBoolean ("I",false);
    knockout = groupAttributesDictionary.getBoolean ("K",false);
    groupAttributesDictionary.checkUnused ("7.13");
  }
  
  public TransparencyGroupAttributes (PDFObject blendingColorSpace,boolean isolated,boolean knockout)
  {
    this.blendingColorSpace = blendingColorSpace;
    this.isolated = isolated;
    this.knockout = knockout;
  }

  public boolean doesFancyBlend () {
    return !(blendingColorSpace instanceof PDFName && ((PDFName) blendingColorSpace).getName ().equals ("DeviceRGB"));
  }
}
