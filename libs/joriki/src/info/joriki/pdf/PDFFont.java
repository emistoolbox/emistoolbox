/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

import info.joriki.io.Resources;
import info.joriki.io.SeekableByteArray;
import info.joriki.io.Util;
import info.joriki.cff.CFFFontSet;
import info.joriki.font.Font;
import info.joriki.font.DescribedFont;
import info.joriki.font.GlyphCanonicalizer;
import info.joriki.font.GlyphDescriptor;
import info.joriki.font.GlyphProvider;
import info.joriki.font.FontSpecification;
import info.joriki.graphics.Rectangle;
import info.joriki.sfnt.SFNTFile;
import info.joriki.util.General;
import info.joriki.util.Options;
import info.joriki.util.Interval;
import info.joriki.util.Assertions;
import info.joriki.util.NotImplementedException;
import info.joriki.util.NotTestedException;
import info.joriki.adobe.Encoding;
import info.joriki.adobe.GlyphList;
import info.joriki.adobe.ToUnicodeMap;
import info.joriki.type1.MultipleMasterFontFile;

abstract public class PDFFont implements Font, PDFOptions
{
  final static Map standardGlyphMap = new HashMap ();
  static {
    // PDFDocEncoding encodes the entire Latin character set
    addStandardGlyphs (Encoding.PDFDocEncoding.glyphs);
    addStandardGlyphs (Encoding.symbolEncoding.glyphs);
  }
  static void addStandardGlyphs (String [] glyphs) {
    for (int i = 0;i < glyphs.length;i++)
      if (glyphs [i] != null)
	standardGlyphMap.put (glyphs [i],new Character (GlyphList.glyphList.getUnicode (glyphs [i])));
  }

  final static String [] multipleMasterFontNames = {"ZX______.PFB","ZY______.PFB"};
  static MultipleMasterFontFile [] multipleMasterFonts =
    new MultipleMasterFontFile [2];

  final static int subsetLength = 6;

  final static int FIXEDPITCH  = 0;
  final static int SERIF       = 1;
  final static int SYMBOLIC    = 2;
  final static int SCRIPT      = 3;
  final static int NONSYMBOLIC = 5;
  final static int ITALIC      = 6;
  final static int ALLCAP      = 16;
  final static int SMALLCAP    = 17;
  final static int FORCEBOLD   = 18;

  String name;
  Encoding encoding;
  ToUnicodeMap toUnicodeMap;
  
  PDFStream stream;
  PDFDictionary fontDictionary;
  PDFDictionary fontDescriptor;

  public int missingWidth;
  
  public boolean vertical;
  
  public boolean symbolic;
  public boolean fixedPitch;
  public boolean serif;
  public boolean italic;
  public boolean nonSymbolic;
  public boolean script;
  public boolean allCap;
  public boolean smallCap;
  public boolean forceBold;

  // for synthesized fonts
  double verticalStemWidth;
  double [] fontMatrix;
  final static double unit = .001;

  final static Set standardFontFamilies = new HashSet ();
  final static Map fontSubstitutionMap = new HashMap ();
  static {
    standardFontFamilies.add ("Times");
    standardFontFamilies.add ("Courier");
    standardFontFamilies.add ("Helvetica");
    standardFontFamilies.add ("Symbol");
    standardFontFamilies.add ("ZapfDingbats");

    fontSubstitutionMap.put ("Arial","Helvetica");
    fontSubstitutionMap.put ("TimesNewRoman","Times");
    fontSubstitutionMap.put ("CourierNew","Courier");
  }

  public static PDFFont getInstance
    (PDFObject fontObject,ResourceResolver resourceResolver)
  {
    PDFDictionary fontDictionary = (PDFDictionary) fontObject;
    PDFDictionary fontDescriptor = (PDFDictionary) fontDictionary.get ("FontDescriptor");
    
    if (fontDictionary.isOfSubtype ("TrueType") &&
        !(fontDictionary.contains ("Widths") &&
          fontDictionary.contains ("FontDescriptor") &&
          fontDescriptor.contains ("FontFile2")))
      {
        // Some files give "TrueType" as the subtype of standard fonts
        // and then don't specify widths, which is technically not allowed.
        // This also happens with Arial, TimesNewRoman and CourierNew. We map
        // Arial to Helvetica, TimesNewRoman to Times and CourierNew to Courier,
        // and then convert all the standard fonts to Type 1 fonts.
        String baseFont = fontDictionary.getUTFName ("BaseFont");
        FontSpecification fontSpecification = new FontSpecification (baseFont);
        String substitute = (String) fontSubstitutionMap.get
          (fontSpecification.family);
        if (substitute != null)
          fontSpecification.family = substitute;
        if (standardFontFamilies.contains (fontSpecification.family))
          {
            fontSpecification.standardize ();
            String standardName = fontSpecification.toPostScriptName ();
            fontDictionary.put ("Subtype","Type1");
            fontDictionary.put ("BaseFont",standardName);
            if (fontDescriptor != null)
              fontDescriptor.put ("FontName",standardName);
        }
        else if (!fontDictionary.contains ("Widths"))
          throw new Error ("Unknown widthless TrueType font " + baseFont);
      }

    fontDictionary.ignore ("FontIdentifier");

    if (fontDictionary.isOfSubtype ("Type1") ||
        fontDictionary.isOfSubtype ("MMType1"))
      return new Type1Font (fontDictionary);
    if (fontDictionary.isOfSubtype ("TrueType"))
      return new TrueTypeFont (fontDictionary);
    if (fontDictionary.isOfSubtype ("Type0"))
      return new Type0Font (fontDictionary);
    if (fontDictionary.isOfSubtype ("Type3"))
      return new Type3Font (fontDictionary);
    throw new NotImplementedException
      ("font type " + fontDictionary.get ("Subtype"));
  }

  // nbyte specifies the number of bytes after decoding
  // encoded text may be variable-length 
  public PDFFont (PDFDictionary fontDictionary,int nbyte)
  {
    this.fontDictionary = fontDictionary;
    Assertions.expect (fontDictionary.isOfType ("Font"));
    // obsolete entry in Tables 5.8 and 5.9;
    // not in Table 5.18 but occurs in Type 0 fonts, too
    fontDictionary.use ("Name");
    name = fontDictionary.getUTFName ("BaseFont");
    PDFStream toUnicodeStream = (PDFStream) fontDictionary.get ("ToUnicode");
    if (toUnicodeStream != null)
      try {
    	  Util.copy (toUnicodeStream.getInputStream ("5.17"),"/Users/joriki/test.txt");
        toUnicodeMap = new ToUnicodeMap (toUnicodeStream.getInputStream ("5.17"),nbyte);
      } catch (IOException ioe) { ioe.printStackTrace (); }
 
    used = new boolean [1 << (nbyte << 3)];
  }

  protected void setFontDescriptor (PDFDictionary container)
  {
    fontDescriptor = (PDFDictionary) container.get ("FontDescriptor");
    
    if (fontDescriptor == null)
    {
      symbolic =
        name.equals ("Symbol") ||
        name.equals ("ZapfDingbats");
      fixedPitch = 
        name.startsWith ("Courier");
      serif =
        name.startsWith ("Courier") ||
        name.startsWith ("Times");
      nonSymbolic = !symbolic;
      italic = name.endsWith ("Italic") || name.endsWith ("Oblique");
      script = false;
      allCap = false;
      smallCap = false;
      forceBold = false;
    }
    else
    {
      String fontName = fontDescriptor.getUTFName ("FontName");
      String baseFont = container.getUTFName ("BaseFont");
      if (!fontName.equals (baseFont))
        Options.warn ("BaseFont and FontName differ" +
        (strip (fontName).equals (strip (baseFont)) ? " by subset" : ""));
      
      int flags = ((PDFInteger) fontDescriptor.get ("Flags")).val;
      symbolic    = ((flags >> SYMBOLIC   ) & 1) != 0;
      fixedPitch  = ((flags >> FIXEDPITCH ) & 1) != 0;
      serif       = ((flags >> SERIF      ) & 1) != 0;
      nonSymbolic = ((flags >> NONSYMBOLIC) & 1) != 0;
      italic      = ((flags >> ITALIC     ) & 1) != 0;
      script      = ((flags >> SCRIPT     ) & 1) != 0;
      allCap      = ((flags >> ALLCAP     ) & 1) != 0;
      smallCap    = ((flags >> SMALLCAP   ) & 1) != 0;
      forceBold   = ((flags >> FORCEBOLD  ) & 1) != 0;
      
      if (symbolic == nonSymbolic)
        Options.warn ("symbolic and non-symbolic flags are both " + (symbolic ? "set" : "clear") + " for font " + name);
      
      verticalStemWidth = fontDescriptor.getDouble ("StemV");
      fontMatrix = new double []
      {unit,0,-unit * Math.tan ((Math.PI / 180) * fontDescriptor.getDouble ("ItalicAngle")),unit,0,0};
    }
  }

  Map codeToDescriptorMap = new HashMap ();
  Map descriptorToCodeMap = new HashMap ();
  // when scrambling Unicodes, we want to fill all
  // one-byte UTF8 sequences first, then the two-byte
  // ones, then the three-byte ones
  final static int [] limits = {0x20,0x80,0x800,0x10000};

  private static char privateUseUnicode (int code)
  {
    return (char) (0xe000 + (code & 0xfff));
  }

  // There can be both an encoding and a ToUnicode map, and they can
  // (in conjunction with the Adobe glyph list) lead to different
  // Unicodes. This is the case on page 1 of tsboat-acc.pdf.
  
  // Before the advent of altGlyphs, if the font was embedded,
  // we used the ToUnicode map (to which the PDF spec gives priority),
  // since we control what glyphs the Unicodes will map to, but if
  // the font was not embedded, we stuck with the Unicodes that result
  // from the encoding, lest we get incorrect or undefined characters.

  // With altGlyphs, we can have the best of both worlds:
  // we always use the encoding to choose the right glyph
  // and the ToUnicode map to choose the right Unicode(s).

  public GlyphDescriptor getGlyphDescriptor (int code)
  {
    Integer theCode = new Integer (code);
    GlyphDescriptor descriptor = (GlyphDescriptor) codeToDescriptorMap.get (theCode);
    if (descriptor == null)
    {
      descriptor = new GlyphDescriptor ();
      if (scrambleUnicodes.isSet ())
      {
        int size = codeToDescriptorMap.size () + limits [0];
        int i = 0;
        while (size >= limits [++i])
          ;
        int base = limits [i-1];
        int range = limits [i] - base;
        do
          descriptor.unicode = base + General.random (range);
        while (codeToDescriptorMap.containsValue (descriptor));
      }
      else
      {
        descriptor = getKnownGlyphDescriptor (code);
        if (descriptor.unicodes == null)
          descriptor.unicodes = new char [] {(char) Math.max (' ',descriptor.unicode != 0 ? descriptor.unicode : code)};
        else {
          // Font F5 of phys32.pdf contains Unicode 1 in ToUnicode map
          // Font C0_3 of Phys-41.pdf contains Unicode 3 as the second part of a strange ligature
          // This is also mapped to the space character on copying to the clipboard from Adobe Reader
          char [] unicodes = descriptor.unicodes;
          for (int i = 0;i < unicodes.length;i++)
            if (unicodes [i] < ' ') {
              if (descriptor.unicodes == unicodes)
                descriptor.unicodes = unicodes.clone ();
              descriptor.unicodes [i] = ' ';
            }
        }

        if (encoding != null)
          descriptor.unicode = encoding.getUnicode (code);
        if (descriptor.unicode == 0)
          descriptor.unicode = getGuessedUnicode (code);
        // we need to replace 3-byte Unicodes for two temporary reasons:
        // Neither the SVG Viewer, nor Batik handles them properly.
        // We'd need to use a cmap in the CEF font that can handle them. 
        if (descriptor.unicode < ' ' || descriptor.unicode >= 0x10000)
          descriptor.unicode = privateUseUnicode (code);
        if (wasValidlyEmbedded ()) {
          Integer oldCode = (Integer) descriptorToCodeMap.get (descriptor);
          if (oldCode != null)
            // We used to try to assign the same unicode to different codes that resulted in the same "glyph representation":
            // getGlyphRepresentation (oldCode.intValue ()).equals (getGlyphRepresentation (code)))
            // This is problematic since e.g. for SVG the glyphs would have to be exactly the same,
            // including advance width and vertical origin. We'd have to check all aspects of the
            // glyphs here that might make a difference, including ones that might be used in the
            // future. Since this would be inherently fragile and there's no longer much value in
            // it since this unicode is now only used internally in SVG and the proper unicodes
            // are provided in an altGlyph tag when necessary, it seems preferable to forego this
            // optimization and just assign a different unicode to each code.
            {
              descriptor.unicode = privateUseUnicode (code);
              while (descriptorToCodeMap.containsKey (descriptor))
                descriptor.unicode++;
              Assertions.limit (descriptor.unicode,0xe000,0xf8ff);
            }
          descriptorToCodeMap.put (descriptor,theCode);
        }
      }
      codeToDescriptorMap.put (theCode,descriptor);
    }
    return descriptor;
  }

  public GlyphDescriptor getKnownGlyphDescriptor (int code) {
    GlyphDescriptor descriptor = new GlyphDescriptor ();
    if (toUnicodeMap != null)
      descriptor.unicodes = toUnicodeMap.getUnicodes (code);
    if (encoding != null) {
      // The spec (p. 441, Section 5.9.1) says that the glyph name
      // is only to be used if *all* glyph names in the encoding
      // are standard. Adobe Reader 7.0 uses any standard glyph
      // name regardless of the others.
      Character unicode = (Character) standardGlyphMap.get (encoding.glyphs [code]);
      if (unicode != null)
        descriptor.unicode = unicode.charValue ();
    }
    descriptor.fill ();
    return descriptor;
  }

  public int getUnicode (int code) {
    return getGlyphDescriptor (code).unicode;
  }

  public int getGuessedUnicode (int code)
  {
    return code;
  }

  boolean [] used;

  public void use (int code,boolean use)
  {
    used [code] = use;
  }

  public void reuse ()
  {
    Arrays.fill (used,false);
    if (scrambleUnicodes.isSet ())
      codeToDescriptorMap.clear ();
  }

  abstract class AbstractCharacterIterator implements CharacterIterator
  {
    double width;
    double advance;

    public int next () {
      int code = nextCode ();
      if (code != -1) {
        width = getGlyphWidth (code);
        advance = getGlyphAdvance (code);
      }
      return code;
    }

    public double getWidth () {
      return width;
    }
    
    public double getAdvance () {
      return advance;
    }

    abstract protected int nextCode ();
  }

  public CharacterIterator getUsedCharacterIterator ()
  {
    return new AbstractCharacterIterator () {
        int code = -1;
        protected int nextCode ()
        {
          try {
            while (!used [++code])
              ;
            return code;
          } catch (ArrayIndexOutOfBoundsException aioobe) {
            return -1;
          }
        }

        public boolean onSpace ()
        {
          throw new UnsupportedOperationException ();
        }
      };
  }

  public boolean isStandardFont ()
  {
    return false;
  }

  public String toString ()
  {
    return name;
  }

  public String getName ()
  {
    return name;
  }

  public PDFStream getStream ()
  {
    return stream;
  }

  public boolean wasEmbedded ()
  {
    return stream != null;
  }

  public boolean wasValidlyEmbedded ()
  {
    return wasEmbedded () && !isInvalid ();
  }
  
  public boolean isEmbedded ()
  {
    return wasValidlyEmbedded () || synthesizeFonts.isSet ();
  }

  public boolean isBullets ()
  {
    return symbolic && !isStandardFont() && !wasValidlyEmbedded();
  }
  
  protected boolean isInvalid () {
    return false;
  }

  public PDFDictionary getDictionary ()
  {
    return fontDictionary;
  }

  public Font getFont ()
  {
    return stream == null ?
      (Font) this :
      (Font) getFontFile ();
  }

  DescribedFont fontFile;
  public DescribedFont getFontFile ()
  {
    if (fontFile == null && stream != null)
      try {
        fontFile = readFontFile ();
      } catch (IOException ioe) { ioe.printStackTrace (); }
    return fontFile;
  }

  public double [] getFontMatrix ()
  {
    Assertions.expect (synthesizeFonts.isSet ());
    Assertions.expect (stream,null);
    return fontMatrix;
  }

  public double [] getFontBBox ()
  {
    Rectangle bbox = fontDescriptor.getRectangle ("FontBBox");
    // if we have individual descriptors for character
    // classes, return the union of all bounding boxes
    PDFDictionary classDescriptors = (PDFDictionary) fontDescriptor.get ("FD");
    if (classDescriptors != null)
      for (PDFObject classDescriptor : classDescriptors.elements ()) {
	Rectangle classBox = ((PDFDictionary) classDescriptor).getRectangle ("FontBBox");
        if (classBox != null)
          bbox.add (classBox);
        throw new NotTestedException ("class-dependent bounding box");
      }
    return bbox.toDoubleArray ();
  }

  Interval widthInterval;

  public Interval getWidthInterval () {
    if (widthInterval == null) {
      widthInterval = new Interval ();
      fillWidthInterval ();
    }
    return widthInterval;
  }
  
  abstract protected void fillWidthInterval ();
  abstract protected double getGlyphWidth (int code);
  abstract protected double getGlyphAdvance (int code);
  abstract protected Object getGlyphSelector (int code);
  abstract protected DescribedFont readFontFile () throws IOException;
  abstract public CharacterIterator getCharacterIterator (byte [] text);
  
  public GlyphProvider getGlyphProvider (int code)
  {
    if (stream != null)
      return getFontFile ().getGlyphProvider (getGlyphSelector (code));
    Assertions.expect (synthesizeFonts.isSet ());
    int which = serif ? 1 : 0;
    MultipleMasterFontFile master = multipleMasterFonts [which];
    if (master == null)
      try {
        master = multipleMasterFonts [which] = new MultipleMasterFontFile
        (Resources.getInputStream (PDFFont.class,multipleMasterFontNames [which]));
      } catch (IOException ioe) {
        ioe.printStackTrace ();
        throw new Error ("couldn't load multiple master font");
      }
    int unicode = getKnownGlyphDescriptor (code).unicode;
    if (unicode == 0)
      throw new Error ("cannot determine unicode for non-embedded glyph");
    if (verticalStemWidth == 0)
      verticalStemWidth = master.getNominalVerticalStemWidth ();
    if (fontDescriptor != null && fontDescriptor.contains ("FD"))
      throw new NotImplementedException ("glyph-dependent synthesis metrics");
    return
      master.getGlyphProvider (GlyphList.getGlyphName (unicode),
			       1000 * getGlyphWidth (code),verticalStemWidth);
  }

  private Object getGlyphRepresentation (int code)
  {
    return isEmbedded() ? new GlyphCanonicalizer (getGlyphProvider (code)) : getGlyphSelector (code);
  }

  // overridden by TrueTypeFont and Type1Font to deal with Length entries
  protected byte [] getStreamData () throws IOException
  {
    return stream.getData ("5.24");
  }
  
  // same as getStreamData (), but handles null stream and catches IOException
  byte [] getRawData ()
  {
    try {
      return stream == null ? null : getStreamData ();
    } catch (IOException ioe) { ioe.printStackTrace (); return null; }
  }

  // This is currently used only in PDFtoCSV.
  // It uses the main font descriptor and ignores the FD entry.
  // We may or may not want to change that.
  public double getHeight ()
  {
    double height = fontDescriptor.getDouble ("CapHeight");
    if (height == 0)
      height = fontDescriptor.getDouble ("Ascent");
    if (height == 0)
      height = fontDescriptor.getRectangleArray ("FontBBox") [3];
    return height / 1000;
  }

  public double getAscent () {
	  return fontDescriptor.getDouble ("Ascent") / 1000;
  }

  public double getDescent () {
	  return fontDescriptor.getDouble ("Descent") / 1000;
  }

  protected DescribedFont readCFFFile () throws IOException
  {
    return new CFFFontSet (getStreamData ()).getOnlyFont ();
  }

  protected DescribedFont readSFNTFile () throws IOException
  {
    return new SFNTFile (new SeekableByteArray (getStreamData ()));
  }

  private static boolean isSubsetName (String name)
  {
    if (name.length () <= subsetLength || name.charAt (subsetLength) != '+')
      return false;
    for (int i = 0;i < subsetLength;i++)
    {
      char c = name.charAt (i);
      if (!('A' <= c && c <= 'Z'))
        return false;
    }
    return true;
  }
  
  // remove subset prefixes.  0306475464_90_113.pdf
  // contains a double prefix, so we need to iterate.
  protected static String strip (String name)
  {
    while (isSubsetName (name))
      name = name.substring (PDFFont.subsetLength + 1);
    return name;
  }
 
  public String getStrippedName ()
  {
    return strip (name);
  }
}
