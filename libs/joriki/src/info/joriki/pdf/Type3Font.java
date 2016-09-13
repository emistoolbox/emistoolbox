/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import info.joriki.font.DescribedFont;
import info.joriki.font.GlyphProvider;

import info.joriki.util.Assertions;
import info.joriki.util.NotImplementedException;
import info.joriki.util.ThreadLocalCount;

import info.joriki.adobe.Encoding;

import info.joriki.graphics.Rectangle;
import info.joriki.graphics.Transformation;

public class Type3Font extends SimpleFont
{
  Transformation fontMatrix;
  double [] fontBBox;
  PDFDictionary charProcs;

  static ThreadLocalCount nameCount = new ThreadLocalCount ();

  public Type3Font (PDFDictionary fontDictionary)
  {
    super (fontDictionary);
    this.name = "Type3Font" + nameCount.nextCount ();
    fontMatrix = new Transformation (fontDictionary.getDoubleArray ("FontMatrix"));
    fontBBox = fontDictionary.getRectangleArray ("FontBBox");
    charProcs = (PDFDictionary) fontDictionary.get ("CharProcs");
    Assertions.expect (fontDictionary.contains ("Encoding"));
    createEncoding ();
    fontDictionary.use ("Resources"); // used in ImageHandler.parse
    fontDictionary.checkUnused ("5.9");
    for (int i = 0;i < 4;i++)
      if (fontBBox [i] != 0)
        return;
    throw new NotImplementedException ("degenerate bounding box");
  }

  protected DescribedFont readFontFile ()
  {
    throw new InternalError ();
  }

  public double getGlyphWidth (int code)
  {
    return getWidthEntry (code) * fontMatrix.matrix [0];
  }

  public boolean wasEmbedded ()
  {
    return true;
  }

  public double [] getFontMatrix ()
  {
    return fontMatrix.matrix;
  }
  
  public double [] getFontBBox ()
  {
    return fontBBox;
  }
                                                     
  public PDFStream getContentStream (int code)
  {
    return (PDFStream) charProcs.get (getName (code));
  }

  public Encoding getDefaultEncoding ()
  {
    return new Encoding ();
  }

  public double getHeight ()
  {
    return new Rectangle (getFontBBox ()).transformBy (fontMatrix).ymax;
  }
  
  public String getName (int code)
  {
    return (String) getGlyphSelector (code);
  }
  
  public GlyphProvider getGlyphProvider (int code)
  {
    throw new InternalError ();
  }
}
