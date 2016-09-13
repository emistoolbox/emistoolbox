/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import info.joriki.util.Assertions;
import info.joriki.util.NotImplementedException;

import info.joriki.graphics.Transformation;

public class PDFPattern
{
  // pattern types
  public final static int TILING = 1;
  public final static int SHADING = 2;
  
  public int type;
  public Transformation matrix;

  PDFPattern (PDFDictionary dictionary,ResourceResolver resourceResolver)
  {
    Assertions.expect (dictionary.isOptionallyOfType("Pattern"));
    type = dictionary.getInt ("PatternType");
    matrix = dictionary.getTransformation ("Matrix",Transformation.identity);
  }

  public static PDFPattern getInstance
    (PDFObject specification,ResourceResolver resourceResolver)
  {
    PDFDictionary dictionary = (PDFDictionary) specification;
    int type = dictionary.getInt ("PatternType");
    switch (type)
      {
      case TILING : return new TilingPattern (dictionary,resourceResolver);
      case SHADING : return new ShadingPattern (dictionary,resourceResolver);
      default : throw new NotImplementedException ("pattern type " + type);
      }
  }
}
