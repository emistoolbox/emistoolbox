/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import info.joriki.util.Assertions;

public class TilingPattern extends PDFPattern
{
  // paint types
  public final static int COLORED = 1;
  public final static int UNCOLORED = 2;

  // tiling types (not used for SVG)
  public final static int CONSTANT_SPACING = 1;
  public final static int NO_DISTORTION = 2;
  public final static int FAST_CONSTANT_SPACING = 3;

  public int paintType;
  public int tilingType;

  public double [] bbox;
  public double xstep;
  public double ystep;

  public PDFStream contentStream;

  TilingPattern (PDFDictionary dictionary,ResourceResolver resourceResolver)
  {
    super (dictionary,resourceResolver);
    contentStream = (PDFStream) dictionary;
    paintType = dictionary.getInt ("PaintType");
    Assertions.expect (paintType == COLORED || paintType == UNCOLORED);
    tilingType = dictionary.getInt ("TilingType");
    Assertions.limit (tilingType,1,3);
    bbox = dictionary.getRectangleArray ("BBox");
    xstep = Math.abs (dictionary.getDouble ("XStep"));
    ystep = Math.abs (dictionary.getDouble ("YStep"));
    Assertions.unexpect (xstep,0);
    Assertions.unexpect (ystep,0);
    dictionary.use ("Resources"); // used in ImageHandler.parse
    dictionary.checkUnused ("4.22");
  }
}
