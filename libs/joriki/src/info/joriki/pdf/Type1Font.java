/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.io.IOException;
import java.io.InputStream;

import java.util.Map;
import java.util.HashMap;

import info.joriki.io.Resources;

import info.joriki.cff.CFFFont;
import info.joriki.cff.CFFFontSet;

import info.joriki.font.DescribedFont;

import info.joriki.util.Assertions;
import info.joriki.util.NotImplementedException;

import info.joriki.adobe.AFMFile;
import info.joriki.adobe.Encoding;

import info.joriki.type1.Type1FontFile;

import info.joriki.charstring.CharStringFont;

public class Type1Font extends NativeSimpleFont implements CFFFontContainer, PDFOptions
{
  final static String standardMetricsDirectory = "fontmetrics";
  final static String standardFontDirectory = "fonts";
  final static Map standardMetricsCache = new HashMap ();
  final static Map standardFontCache = new HashMap ();

  AFMFile AFMfile;

  Type1Font (PDFDictionary fontDictionary)
  {
    super (fontDictionary);

    if (fontDictionary.isOfSubtype ("MMType1"))
      name = name.replace ('_',' ');

    AFMfile = (AFMFile) standardMetricsCache.get (name);
    if (AFMfile == null)
      try {
        InputStream in = Resources.getInputStream (Type1Font.class,standardMetricsDirectory + "/" + name + ".afm");
        if (in != null) {
          AFMfile = new AFMFile (in);
          standardMetricsCache.put (name,AFMfile);
        }
      } catch (IOException ioe) { ioe.printStackTrace (); }

    if (fontDescriptor != null) {
      stream = (PDFStream) fontDescriptor.get ("FontFile3");
      if (stream == null)
        stream = (PDFStream) fontDescriptor.get ("FontFile");
    }
    else if (AFMfile == null)
      throw new Error ("missing font descriptor");

    if (stream == null && AFMfile != null && !omitStandardFonts.isSet ()) {
      stream = (PDFStream) standardFontCache.get (name);
      if (stream == null) {
        try {
          byte [] bytes = Resources.getBytes (Type1Font.class,standardFontDirectory + '/' + name);
          if (bytes != null) {
            stream = new PDFStream (bytes,new String [0]);
            standardFontCache.put (name,stream);
          }
        } catch (IOException ioe) { ioe.printStackTrace (); }
        standardStream = true;
        fontDictionary.ignore ("NativeName"); // Sample-Statement.pdf
      }
    }
    
    createEncoding ();

    if (widths == null)
      {
        if (AFMfile == null)
          throw new Error ("missing width array in non-standard font");
        standardWidths = new float [256];
        for (int i = 0;i < 256;i++)
          {
            String glyph = encoding.glyphs [i];
            if (glyph != null)
              standardWidths [i] = AFMfile.getWidth (glyph);
          }
      }

    fontDictionary.checkUnused ("5.8");
  }

  float [] standardWidths;
  boolean standardStream;

  public double getGlyphWidth (int code)
  {
    return standardWidths == null ?
      super.getGlyphWidth (code) :
      standardWidths [code];
  }

  public Encoding getDefaultEncoding ()
  {
    return getFontFile () != null ?
      ((CharStringFont) fontFile).getDefaultEncoding () :
      name.equals ("Symbol") ?
      Encoding.symbolEncoding :
      name.equals ("ZapfDingbats") ?
      Encoding.dingbatsEncoding :
      super.getDefaultEncoding ();
  }

  public boolean isStandardFont ()
  {
    return AFMfile != null;
  }

  protected DescribedFont readFontFile () throws IOException
  {
    if (stream.isOfSubtype ("Type1C"))
      try {
        return readCFFFile ();
      } catch (NotImplementedException nie) {
        // 0765611651_3.pdf contains a Type 1 font file instead
        // of a CFF file -- this causes a NotImplementedException
        // for an unknown CFF version, which we catch to check
        // whether the file looks like a Type 1 font file.
        // checkUnused has already been called above.
        InputStream in = stream.getInputStream ();
        boolean type1 = in.read () == '%' && in.read () == '!';
        in.close ();
        if (!type1)
          throw nie;
      }
      // checkUnused has already been called above.
      try {
        return new Type1FontFile (stream.getInputStream (),standardStream);
      } catch (NotImplementedException nie1) {
        try {
          // standard fonts in phys-81.pdf are embedded as hexadecimal
          return new Type1FontFile (stream.getInputStream (),!standardStream);
        } catch (NotImplementedException nie2) {
          throw nie1;
        }
      }        
  }
  
  public double getHeight ()
  {
    if (fontDescriptor != null)
      return super.getHeight ();
    if (AFMfile != null)
      return AFMfile.getAscender () / 1000.;
    throw new Error ("missing font descriptor for non-standard font");
  }

  public boolean containsCFF ()
  {
    return stream != null;
  }

  // This is only for Type 1 font files and will throw an exception if used otherwise (CFF or non-embedded). 
  CFFFontSet CFFfontSet;
  CFFFontSet getCFFFontSet ()
  {
    if (CFFfontSet == null)
      CFFfontSet = ((Type1FontFile) getFontFile ()).toCFFFontSet ();
    return CFFfontSet;
  }

  public int getIndex (int code)
  {
    return (getFontFile () instanceof CFFFont ?
            (CFFFont) getFontFile () :
            getCFFFontSet ().getOnlyFont ()).
      GIDfor (encoding.glyphs [code]);
  }

  protected byte [] getStreamData () throws IOException
  {
    if (stream.isOfSubtype("Type1C")) {
      // The spec doesn't specify the extra length entries for CFF fonts.
      // Some (sap13.pdf) have none, some(test2.pdf) have Length1 like TrueType fonts.
      boolean hasLength = stream.contains ("Length1");
      byte [] data = super.getStreamData ();
      if (hasLength)
        Assertions.expect (data.length,stream.getInt ("Length1"));
      return data;
    }
    Assertions.unexpect (stream.contains ("Subtype"));
    stream.getInt ("Length1");
    stream.getInt ("Length2");
    Assertions.unexpect (stream.getInt ("Length3"),0);
    return super.getStreamData ();
  }
  
  public byte [] getCFFData ()
  {
    return stream != null && stream.isOfSubtype ("Type1C") ? getRawData () :
      getCFFFontSet ().toByteArray ();
  }
  
  public boolean containsOverriddenGlyphs () {
    return getFontFile () != null && fontDictionary.contains ("Encoding");
  }
}
