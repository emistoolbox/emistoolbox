/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import info.joriki.adobe.Encoding;

import info.joriki.util.Options;

abstract public class NativeSimpleFont extends SimpleFont
{
  public NativeSimpleFont (PDFDictionary fontDictionary)
  {
    super (fontDictionary);
    setFontDescriptor (fontDictionary);
    if (fontDescriptor != null)
      missingWidth = fontDescriptor.getInt ("MissingWidth",0);
  }

  public double getGlyphWidth (int code)
  {
    return getWidthEntry (code) / 1000;
  }
  
  public Encoding getDefaultEncoding ()
  {
    if (isBullets ())
    {  
      // The spec says (p. 389) that for a non-embedded symbolic font,
      // the font's built-in encoding is to be used. I don't know what
      // this could possibly mean. See also TrueTypeFont.getGlyphSelector,
      // TrueTypeFont.getDefaultEncoding and the check at the end of the
      // TrueTypeFont constructor.
      // Since non-embedded symbolic fonts are now replaced by bullets,
      // this encoding only affects Unicodes, not glyph shapes.
      Options.warn ("using empty default encoding for non-embedded symbolic font");
      return new Encoding ();
    }
    return Encoding.standardEncoding;
  }
}
