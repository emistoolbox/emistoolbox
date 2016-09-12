/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import info.joriki.util.Assertions;
import info.joriki.util.General;

import info.joriki.adobe.DefaultEncodingProvider;
import info.joriki.adobe.Encoding;

abstract public class SimpleFont extends PDFFont implements DefaultEncodingProvider // getEncoding () provides the default encoding
{
  PDFArray widths;
  int firstChar;
  int lastChar;
  
  public SimpleFont (PDFDictionary fontDictionary)
  {
    super (fontDictionary,1);
    widths = (PDFArray) fontDictionary.get ("Widths");
    if (widths != null)
    {
      firstChar = fontDictionary.getInt ("FirstChar");
      lastChar = fontDictionary.getInt ("LastChar");
    }
    else
    {
      // XMLspyIDE.pdf
      fontDictionary.ignore ("FirstChar");
      fontDictionary.ignore ("LastChar");
    }
  }

  public double getWidthEntry (int code)
  {
    return firstChar <= code && code <= lastChar ?
      widths.doubleAt (code - firstChar) : missingWidth;
  }

  public double getGlyphAdvance (int code) {
    return getGlyphWidth (code);
  }
  
  public CharacterIterator getCharacterIterator (final byte [] text)
  {
    return new AbstractCharacterIterator () {
        int n = 0;

        protected int nextCode ()
        {
          return n < text.length ? text [n++] & 0xff : -1;
        }

        public boolean onSpace ()
        {
          return text [n - 1] == ' ';
        }
      };
  }

  protected void createEncoding ()
  {
    encoding = createEncoding (fontDictionary.get ("Encoding"),this);
  }
  
  static Encoding createEncoding (PDFObject encodingObject,DefaultEncodingProvider defaultEncodingProvider) {
    if (encodingObject instanceof PDFName)
      return Encoding.getEncoding (((PDFName) encodingObject).getName ());
    
    if (encodingObject instanceof PDFDictionary)
    {
      PDFDictionary encodingDictionary = (PDFDictionary) encodingObject;
      Assertions.expect (encodingDictionary.isOptionallyOfType("Encoding"));
      PDFName baseEncodingName = (PDFName) encodingDictionary.get ("BaseEncoding");
      PDFArray differences = (PDFArray) encodingDictionary.get ("Differences");
      encodingDictionary.checkUnused ("5.11");
      
      Encoding encoding = baseEncodingName == null ? defaultEncodingProvider.getDefaultEncoding () :
      Encoding.getEncoding (baseEncodingName.getName ());
        
      if (differences != null)
      {  
        encoding = (Encoding) encoding.clone ();
        for (int i = 0,index = 0;i < differences.size ();i++)
        {
          PDFObject object = differences.get (i);
          if (object instanceof PDFInteger)
            index = ((PDFInteger) object).val;
          else
            encoding.glyphs [index++] = ((PDFName) object).getName ();
        }
      }
      return encoding;
    }
    
    if (encodingObject == null)
      return defaultEncodingProvider.getDefaultEncoding ();

    throw new IllegalArgumentException (encodingObject.toString ());
  }

  public int getGuessedUnicode (int code)
  {
    if (encoding != null)
      {
        // Since only standard glyph names are allowed
        // in determining known unicodes, try others now.
  		int unicode = encoding.getUnicode (code);
  	  	if (unicode != 0)
  	  	  return unicode;
        // try extracting numbered glyphs (Gxx / Cdd)
        String glyph = encoding.glyphs [code];
        if (glyph != null && glyph.length () > 0) // 158053547X.pdf
          {
            char first = glyph.charAt (0);
            if (first == 'C' || first == 'G') {
              boolean hex = first == 'G';
              int index = General.numberIndex (glyph,1,false,hex);
              if (index != 1)
                return Integer.parseInt (glyph.substring (1,index),hex ? 16 : 10);
            }
          }
      }

    return super.getGuessedUnicode (code);
  }

  // overridden for embedded TrueType fonts
  protected Object getGlyphSelector (int code)
  {
    return encoding.glyphs [code];
  }

  public void fillWidthInterval () {
    for (int code = 0;code < 256;code++)
      widthInterval.add (getGlyphWidth (code));
  }
}
