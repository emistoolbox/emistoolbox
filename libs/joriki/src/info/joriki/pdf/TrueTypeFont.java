/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.io.IOException;

import java.util.Map;

import info.joriki.font.DescribedFont;

import info.joriki.sfnt.SFNTFile;
import info.joriki.sfnt.SFNTSpeaker;
import info.joriki.sfnt.CMap;
import info.joriki.sfnt.CMapTable;
import info.joriki.sfnt.PostScriptTable;

import info.joriki.util.NotImplementedException;
import info.joriki.util.Range;
import info.joriki.util.Options;
import info.joriki.util.Assertions;

import info.joriki.adobe.Encoding;

public class TrueTypeFont extends NativeSimpleFont implements SFNTSpeaker
{
  boolean usePostScriptTable;
  boolean mapSymbolically;

  TrueTypeFont (PDFDictionary fontDictionary)
  {
    super (fontDictionary);

    Assertions.unexpect (fontDescriptor,null);
    
    stream = (PDFStream) fontDescriptor.get ("FontFile2");

    PDFObject encodingObject = fontDictionary.get ("Encoding");
    boolean hasEncoding = encodingObject != null;
    
    if (hasEncoding)
      createEncoding ();

    mapSymbolically = symbolic || !hasEncoding;

    // If (mapSymbolically && hasEncoding), the encoding is ignored. This is
    // specified in the last part of subsection "Encodings for TrueType Fonts"
    // of Section 5.5.5 Character Encoding. This case didn't occur for years;
    // the first occurrence was in oraclewhitepapers-academicenterprise.pdf.

    boolean mapNonSymbolically = nonSymbolic || encodingObject instanceof PDFName;

    // The PDF Reference is ambiguous on how to map character codes to TrueType indices.
    // It specifies that one mapping algorithm is to be applied if the font is symbolic
    // or has no encoding (mapSymbolically) and that another algorithm is to be applied
    // if the font is non-symbolic or has a standard encoding (mapNonSymbolically).
    // The first file to contain a case in which both conditions are fulfilled was
    // Inda-MFD2007_1.pdf, for the trademark character in the font named R12;
    // it turns out that the symbolic mapping has priority.
    
    if (!mapSymbolically && !mapNonSymbolically)
      throw new NotImplementedException ("undocumented mapping algorithm for TrueType fonts");

    // We used to check for non-embedded symbolic TrueType fonts here:
    // if (mapSymbolically && stream == null)
    // sp-ultramax-inlinedrives.pdf contains a case of this, in which
    // ZapfDingbatsBT on p. 108 is displayed as bullets in the Reader.
    // The following is a comment from before this case occurred:
      // ---------------------------------------------------------------
      // This should not happen according to the guidelines in the spec.
      // If it does, getGlyphSelector would currently throw an exception.
      // However, it would only be called to resolve Unicode conflicts,
      // which could only arise if there is a ToUnicode map. In this case
      // it should probably just return the corresponding unicode. In any case,
      // it would be good to see how the Adobe Reader chooses glyphs in this case.
      // non-embedded_symbolic_TrueType.pdf contains an artificial example.
      // Here Adobe Reader complains if the font isn't installed, and seems
      // to use WinAnsiEncoding if it is; I didn't investigate whether this
      // depends on the cmaps in the installed font file.
      // See also NativeSimpleFont.getDefaultEncoding ().
      // ---------------------------------------------------------------
    // Since we can't know whether a font will be installed and
    // glyph selection from it would be unclear anyway, the best
    // we can do is to always show bullets, which is also easiest
    // since it's what we do for other non-embedded symbolic fonts.
    
    fontDictionary.checkUnused ("5.8");
  }

  public Encoding getDefaultEncoding ()
  {
    Options.warn ("customized encoding for TrueType font");
    usePostScriptTable = restoreGlyphs.isSet ();
    // This is used only for non-symbolical mapping.
    // The spec says that any undefined entries in the encoding are
    // taken from the standard encoding *after* the encoding is
    // constructed. Since WinAnsiEncoding and MacRomanEncoding
    // don't have any undefined entries that are defined in the
    // standard encoding, this is only relevant in the case where
    // Encoding is a dictionary that specifies MacExpertEncoding
    // as the base encoding. However, in this case the Adobe Reader
    // doesn't actually fill in the undefined entries (I tried \303)
    // but shows empty glyphs. Thus the Reader's behaviour is reproduced
    // if we start out with the standard encoding as the default encoding.
    return super.getDefaultEncoding ();
  }

  protected DescribedFont readFontFile () throws IOException
  {
    return readSFNTFile ();
  }

  protected boolean isInvalid () {
    return ((SFNTFile) getFontFile ()).getHeader () == null;
  }

  public byte [] getStreamData () throws IOException
  {
    int length = stream.getInt ("Length1");
    byte [] data = super.getStreamData ();
    switch (data.length - length) {
    case 0 : break;
    case 1 : Options.warn ("font stream length off by one"); break; // nickjr20050401-ae_35.pdf
    default: throw new Error ("incorrect font stream length");
    }
    return data;
  }
  
  Map macOS;
  CMapTable cmapTable;
  PostScriptTable postScriptTable;
  boolean initializedGlyphSelection;

  final static char prefixMask = 0xff00;
  Range codeDomain;

  public Object getGlyphSelector (int code)
  {
	  System.out.println ("selecting glyph for " + Integer.toHexString (code) + " from " + getName ());
	  
    if (stream == null)
      // only reached for unicode conflict resolution
      return super.getGlyphSelector(code);

	  if (!initializedGlyphSelection)
    {
		  System.out.println ("initializing glyph selection ");
      SFNTFile sfntFile = (SFNTFile) getFontFile ();
      CMap cmap = (CMap) sfntFile.getTable (CMAP);
      if (cmap != null)
      {
        if (mapSymbolically)
        {
          cmapTable = cmap.getTable (3,0);
          if (cmapTable != null)
          {
            codeDomain = cmapTable.getDomain ();
            if ((codeDomain.beg & prefixMask) !=
              (codeDomain.end & prefixMask))
              Options.warn ("invalid code domain in symbolic TrueType font");
          }
        }
        else
          cmapTable = cmap.getTable (3,1);
        
        if (cmapTable == null)
          cmapTable = cmap.getTable (1,0);
      }
      if (!mapSymbolically)
      {
        postScriptTable = (PostScriptTable) sfntFile.getTable (POST);
        if (cmapTable != null && cmapTable.platform == 1)
          macOS = Encoding.macOSEncoding.getGlyphToCodeMap ();
      }
      
      initializedGlyphSelection = true;
    }
    
    int index = 0;
    if (cmapTable != null)
    {
      int decoded;
      if (mapSymbolically)
      {
        decoded = code;
        if (codeDomain != null)
        {
          decoded += codeDomain.beg & prefixMask;
          if (!codeDomain.contains (decoded))
          {
            // 1575056801.pdf contains a cmap that maps only 0xffff
            if (codeDomain.beg == 0xffff)
              Options.warn ("font contains no valid code mappings");
            else
              throw new Error ("character code outside cmap domain");
          }
        }
      }
      else if (cmapTable.platform == 1)
      {
        Character c = (Character) macOS.get (encoding.glyphs [code]);
        decoded = c == null ? 0 : c.charValue ();
      }
      else if (cmapTable.platform == 3)
        // The spec (p. 401) says to use "the Adobe Glyph List",
        // but Adobe Reader 7.0 uses the entire mapping algorithm
        decoded = encoding.getUnicode (code);
      else
        throw new InternalError ();
      
      if (decoded != 0 || mapSymbolically)
        index = cmapTable.getGlyphIndex (decoded);
    }

    if ((usePostScriptTable || index == 0) &&
        postScriptTable != null &&
        encoding.glyphs [code] != null)
      index = postScriptTable.getGlyphIndex (encoding.glyphs [code]);

    return new Integer (index);
  }
}
