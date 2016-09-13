/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

public class ShadingPattern extends PDFPattern
{
  public PDFShading shading;
  public PDFDictionary graphicsStateDictionary;

  ShadingPattern (PDFDictionary dictionary,ResourceResolver resourceResolver)
  {
    super (dictionary,resourceResolver);
    shading = (PDFShading) resourceResolver.getCachedObject
      (ObjectTypes.SHADING,dictionary.get ("Shading"));
    graphicsStateDictionary = (PDFDictionary) dictionary.get ("ExtGState");
    dictionary.checkUnused ("4.23");
  }
}
