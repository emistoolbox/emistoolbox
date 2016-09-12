/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.cff;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import info.joriki.io.Util;
import info.joriki.io.FullySeekableDataInput;

import info.joriki.font.InvalidGlyphException;
import info.joriki.font.TransformedGlyphProvider;
import info.joriki.font.Widths;
import info.joriki.font.FontEncoder;
import info.joriki.font.GlyphProvider;
import info.joriki.font.DescribedFont;

import info.joriki.util.NotTestedException;
import info.joriki.util.Range;
import info.joriki.util.General;
import info.joriki.util.Options;
import info.joriki.util.Assertions;
import info.joriki.util.SmartSoftReference;
import info.joriki.util.NotImplementedException;

import info.joriki.adobe.Encoding;
import info.joriki.adobe.GlyphList;

import info.joriki.charstring.CharStringFont;
import info.joriki.charstring.Type2CharStringEncoder;
import info.joriki.charstring.Type2CharStringDecoder;

import info.joriki.graphics.Point;
import info.joriki.graphics.Rectangle;
import info.joriki.graphics.Transformation;

public class CFFFont extends CFFDict
  implements CFFSpeaker,CharStringFont,FontEncoder,DescribedFont
{
  final static boolean bookCompatibility = false;
  final static short UNENCODED = -1;
  final static int nISOAdobe = 229;
  
  public CFFDict privateDict;

  List glyphs;
  short [] encoding;
  String [] supplements;
  Encoding predefinedEncoding;
  String [] predefinedCharset;
  CFFIndex charStrings = new CFFIndex ();
  CFFIndex subrIndex;

  public final static String notdef = ".notdef";
  final static byte [] defaultNotdef = {(byte) 139,14};

  boolean isCID;

  // data for CID fonts -- should maybe be a class for itself?
  byte [] selectArray;
  int [] selectGIDs;
  int [] selectIndices;
  CFFDict [] fontDicts;
  CFFIndex [] selectSubrs;
  CFFDict [] privateDicts;
  Transformation [] glyphTransforms;
  
  FontData data = new FontData ();

  void registerGlyphString (int GID,int ID)
  {
    setGlyphName (GID,isCID ?
                  (Object) new Integer (ID) : 
                  (Object) fontSet.getString (ID));
  }

  CFFDict makePrivateDict ()
  {
    return new CFFDict (fontSet,privateOps,privateDefaults);
  }

  class FontData implements CFFObject
  {
    void readFrom (FullySeekableDataInput in) throws IOException
    {
      isCID = get ("ROS") != null;

      if (seek ("CharStrings",in))
        charStrings.readFrom (in);

      int charsetOffset = ((Integer) get ("charset")).intValue ();
      if (Options.tracing && charsetOffset < 3)
        System.err.println ("using predefined charset " + charsetOffset);
      switch (charsetOffset)
        {
        case 0 : predefinedCharset = standardStrings; break;
        case 1 : predefinedCharset = expertCharset; break;
        case 2 : predefinedCharset = expertSubset; break;
        default :
          in.seek (charsetOffset);
          int format = in.read (); // format of charset specification
          int n = charStrings.size ();
          if (n != 0)
            {
              glyphs = new ArrayList (n);
              int GID = 1;

              switch (format)
                {
                case 0 :
                  while (GID < n)
                    registerGlyphString (GID++,in.readUnsignedShort ());
                  break;
                case 1 :
                case 2 :
                  while (GID < n)
                    {
                      int first = in.readUnsignedShort ();
                      int left = format == 1 ? in.read () : in.readUnsignedShort ();
                      for (int SID = first;SID <= first + left;SID++)
                        registerGlyphString (GID++,SID);
                    }
                  break;
                default : throw new Error ("unknown charset format " + format);
                }
            }
        }
      
      if (isCID)
        {
          privateDict = null; // try not to generate this in the first place
          in.seek (((Integer) get ("FDSelect")).intValue ());
          int format = in.read ();
          switch (format)
            {
            case 0 :
              selectArray = Util.readBytes (in,charStrings.size ());
              break;
            case 3 : 
              int nrange = in.readUnsignedShort ();
              selectGIDs = new int [nrange+1];
              selectIndices = new int [nrange];
              for (int i = 0;i <= nrange;i++)
                {
                  selectGIDs [i] = in.readUnsignedShort ();
                  if (i != nrange)
                    selectIndices [i] = in.read ();
                }
              break;
            default :
              throw new NotImplementedException ("FDSelect format " + format);
            }
          in.seek (((Integer) get ("FDArray")).intValue ());
          CFFIndex fontDictIndex = new CFFIndex (in);
          fontDicts = new CFFDict [fontDictIndex.size ()];
          selectSubrs = new CFFIndex [fontDictIndex.size ()];
          privateDicts = new CFFDict [fontDictIndex.size ()];
          glyphTransforms = new Transformation [fontDictIndex.size ()];
          Number [] globalFontMatrix = null;
          for (int i = 0;i < fontDictIndex.size ();i++)
            {
              fontDicts [i] = new CFFDict (fontSet,fontDictOps);
              byte [] dict = (byte []) fontDictIndex.get (i);
              fontDicts [i].readFrom (new ByteArrayInputStream (dict));
              privateDicts [i] = makePrivateDict ();
              selectSubrs [i] = readPrivateDict
                (privateDicts [i],fontDicts [i],in);
              
              Number [] fontMatrix = (Number []) fontDicts [i].get ("FontMatrix");
              if (fontMatrix == null)
                fontMatrix = (Number []) get ("FontMatrix");
              if (globalFontMatrix == null)
                globalFontMatrix = fontMatrix;
              else if (!Arrays.equals (fontMatrix,globalFontMatrix)) {
                glyphTransforms [i] = new Transformation (toTransformation (fontMatrix),toTransformation (globalFontMatrix).inverse ());
                if (!glyphTransforms [i].isScaling ())
                  throw new NotTestedException ();
              }
            }
          put ("FontMatrix",globalFontMatrix);
        }
      else
        {
          int encodingOffset = ((Integer) get ("Encoding")).intValue ();
          switch (encodingOffset)
            {
            case 0  : predefinedEncoding = Encoding.standardEncoding; break;
            case 1  : predefinedEncoding = new Encoding (expertStrings); break;
            default :
              in.seek (encodingOffset);
              // Encoding now uses shorts to allow for an UNENCODED value.
              // Codes are still 1-byte; the following is about 2-byte GIDs:
              // I used to have encoding = new byte [256].
              // We need 257 in the case which the CFF spec
              // gives as an example for a full contiguous range, [0 255]
              // Since GID 0 is unencoded, this makes GIDs 1 to 256
              // encoded by 0 to 255. This occurs in horse.pdf (page 6,
              // font Modula), though the built-in encoding isn't used there.
              // This seems to imply that in principle CFF fonts can have
              // two-byte GIDs, but this is the only case I've seen where
              // this actually happens. Anyway, since the charset takes
              // the number of GIDs from the size of charStrings, why not
              // do that here.
              encoding = new short [charStrings.size ()];
              int format = in.read ();
              int n = in.read ();
              int GID = 1;
              switch (format & ~0x80)
                {
                case 0 :
                  for (int i = 0;i < n;i++)
                    encoding [GID++] = (short) in.read ();
                  break;
                case 1 :
                  for (int i = 0;i < n;i++)
                    {
                      int first = in.read ();
                      int left = in.read ();
                      for (int j = first;j <= first + left;j++)
                        encoding [GID++] = (short) (j & 0xff);
                    }
                  break;
                default : throw new Error ("unknown encoding format " + format);
                }

              while (GID < encoding.length)
                encoding [GID++] = UNENCODED;
              
              if ((format & 0x80) != 0)
                {
                  supplements = new String [256];
                  int nsupplements = in.read ();
                  for (int i = 0;i < nsupplements;i++)
                    {
                      int code = in.read ();
                      int SID = in.readUnsignedShort ();
                      supplements [code] = fontSet.getString (SID);
                    }
                }
            }
    
          subrIndex = readPrivateDict (privateDict,CFFFont.this,in);
        }
    }

    // reads private dict, returns its subrs index
    private CFFIndex readPrivateDict
      (CFFDict privateDict,CFFDict dad,FullySeekableDataInput in)
      throws IOException
    {
      Number [] privateInfo = (Number []) dad.get ("Private");
      Assertions.unexpect (privateInfo,null);
      int privateLength = privateInfo [0].intValue ();
      int privateOffset = privateInfo [1].intValue ();
      in.seek (privateOffset);
      privateDict.readFrom (new ByteArrayInputStream
        (Util.readBytes (in,privateLength)));
      Integer subrsOffset = (Integer) privateDict.get ("Subrs");
      if (subrsOffset == null)
        return new CFFIndex ();
      in.seek (privateOffset + subrsOffset.intValue ());
      return new CFFIndex (in);
    }

    public void writeTo (ByteArrayOutputStream baos)
    {
      if (isCID)
        throw new NotImplementedException ("Writing CID fonts");

      if (subrIndex != null && !subrIndex.isEmpty ())
        {
          if (privateDict == null)
            privateDict = makePrivateDict ();
          privateDict.put ("Subrs",new Integer (baos.size ()));
          subrIndex.writeTo (baos);
        }

      if (encoding == null)
        {
          if (predefinedEncoding == null ||
              predefinedEncoding == Encoding.standardEncoding)
            remove ("Encoding");
          else if (predefinedEncoding.glyphs == expertStrings)
            put ("Encoding",new Integer (1));
          else
            throw new Error ("unknown predefined Encoding");
        }
      else
        {
          put ("Encoding",new Integer (baos.size ()));
          if (encoding.length == 0)
            {
              baos.write (0);
              baos.write (0);
            }
          else
            {
              int nsupplements = 0;
              if (supplements != null)
                for (String supplement : supplements)
                  if (supplement != null)
                    nsupplements++;

              baos.write (nsupplements == 0 ? 1 : 0x81);
              List ranges = new ArrayList ();
  
              int limit = Math.min (encoding.length,charStrings.size ());

              for (int i = 1;i < limit;)
                {
                  int beg = encoding [i];
                  if (beg == UNENCODED) {
                    i++;
                    throw new NotTestedException ("unencoded glyph in CFF font");
//                    continue;
                  }
                  int end = beg;
                  do
                    end++;
                  while (++i < limit && encoding [i] == end);
                  ranges.add (new Range (beg,end));
                }

              int n = ranges.size ();
              baos.write (n);
              for (int i = 0;i < n;i++)
                {
                  Range range = (Range) ranges.get (i);
                  baos.write (range.beg);
                  baos.write (range.end - range.beg - 1);
                }

              if (nsupplements != 0)
                {
                  baos.write (nsupplements);
                  for (int i = 0;i < supplements.length;i++)
                    if (supplements [i] != null)
                      {
                        int SID = fontSet.getSID (supplements [i]);
                        baos.write (i);
                        baos.write (SID >> 8);
                        baos.write (SID);
                      }
                }
            }
        }

      if (privateDict == null)
        remove ("Private");
      else
        {
          int beg = baos.size ();
          privateDict.writeTo (baos);
          int end = baos.size ();
          put ("Private",new Number [] {new Integer (end - beg),new Integer (beg)});
        }

      if (bookCompatibility && glyphs == null && predefinedCharset == null)
        // OK for viewing PDF, but causes problems
        // if the PDF file is printed.
        {
          glyphs = new ArrayList (encoding.length);
          glyphs.add (notdef);
          for (int GID = 1;GID < encoding.length;GID++)
            glyphs.add (GID,"C" + GID);
        }

      if (glyphs == null)
        {
          if (predefinedCharset == standardStrings)
            remove ("charset");
          else if (predefinedCharset == expertCharset)
            put ("charset",new Integer (1));
          else if (predefinedCharset == expertSubset)
            put ("charset",new Integer (2));
          else
            throw new Error ("unknown charset " + predefinedCharset);
        }
      else
        {
          put ("charset",new Integer (baos.size ()));
          baos.write (0);
          int GID = 1;
          for (;GID < glyphs.size ();GID++)
            {
              String glyph = (String) glyphs.get (GID);
              int SID = glyph == null ? 0 : fontSet.getSID (glyph);
              baos.write (SID >> 8);
              baos.write (SID);
            }
          while (GID++ < charStrings.size ())
            {
              baos.write (0);
              baos.write (0);
            }
        }

      if (charStrings.size () == 0 || charStrings.get (0) == null)
        setCharString (0,defaultNotdef);
      // required
      put ("CharStrings",new Integer (baos.size ()));
      charStrings.writeTo (baos);
    }
  }

  final static HashMap fontDefaults = new HashMap ();
  final static HashMap privateDefaults = new HashMap ();
  static {
    Number zero = new Integer (0);
    Number thousandth = new Double (.001);
    fontDefaults.put ("Encoding",zero);
    fontDefaults.put ("charset",zero);
    fontDefaults.put ("isFixedPitch",zero);
    fontDefaults.put ("ItalicAngle",zero);
    fontDefaults.put ("UnderlinePosition",new Integer (-100));
    fontDefaults.put ("UnderlineThickness",new Integer (50));
    fontDefaults.put ("PaintType",zero);
    fontDefaults.put ("CharstringType",new Integer (2));
    fontDefaults.put ("StrokeWidth",zero);
    fontDefaults.put ("FontBBox",new Number [] {zero,zero,zero,zero});
    fontDefaults.put ("FontMatrix",new Number [] {thousandth,zero,zero,
                                                  thousandth,zero,zero});
    fontDefaults.put ("CIDFontVersion",zero);
    fontDefaults.put ("CIDFontRevision",zero);
    fontDefaults.put ("CIDFontType",zero);
    fontDefaults.put ("CIDCount",new Integer (8720));
    privateDefaults.put ("BlueScale",new Double (0.039625));
    privateDefaults.put ("BlueShift",new Integer (7));
    privateDefaults.put ("BlueFuzz", new Integer (1));
    privateDefaults.put ("ForceBold",zero);
    privateDefaults.put ("LanguageGroup",zero);
    privateDefaults.put ("ExpansionFactor",new Double (0.06));
    privateDefaults.put ("initialRandomSeed",zero);
    privateDefaults.put ("defaultWidthX",zero);
    privateDefaults.put ("nominalWidthX",zero);
  }
    
  public CFFFont (CFFFontSet fontSet)
  {
    super (fontSet,fontOps,fontDefaults);
    privateDict = makePrivateDict ();
  }

  public int GIDfor (Object glyph)
  {
    Integer GID = (Integer) getGlyphToIndexMap ().get (glyph);
    return GID == null ? 0 : GID.intValue ();
  }

  boolean seek (String key,FullySeekableDataInput in) throws IOException
  {
    Integer off = (Integer) get (key);
    if (off == null)
      return false;
    in.seek (off.intValue ());
    return true;
  }

  double [] getDoubleArray (String key)
  {
    return toDoubleArray ((Number []) get (key));
  }
  
  double [] toDoubleArray (Number [] numbers) {
    if (numbers == null)
      return null;
    double [] result  = new double [numbers.length];
    for (int i = 0;i < result.length;i++)
      result [i] = numbers [i].doubleValue ();
    return result;
  }
  
  Transformation toTransformation (Number [] numbers) {
    return new Transformation (toDoubleArray (numbers));
  }

  public double [] getFontMatrix ()
  {
    return getDoubleArray ("FontMatrix");
  }

  public double [] getFontBBox ()
  {
    return getDoubleArray ("FontBBox");
  }

  Rectangle getBoundingRectangle ()
  {
    double [] fontBBox = getFontBBox ();
    return fontBBox == null ? new Rectangle () : new Rectangle (fontBBox);
  }

  void setBoundingRectangle (Rectangle rectangle)
  {
    setFontBBox (rectangle.toDoubleArray ());
  }

  public void addBoundingBox (double [] arr)
  {
    Rectangle boundingRectangle = getBoundingRectangle ();
    boundingRectangle.add (new Rectangle (arr));
    setBoundingRectangle (boundingRectangle);
  }

  void putDoubleArray (String key,double [] arr)
  {
    Number [] numbers = new Number [arr.length];
    for (int i = 0;i < numbers.length;i++)
      numbers [i] = new Double (arr [i]);
    put (key,numbers);
  }

  public void setFontMatrix (double [] fontMatrix)
  {
    putDoubleArray ("FontMatrix",fontMatrix);
  }

  public void setFontBBox (double [] fontBBox)
  {
    putDoubleArray ("FontBBox",fontBBox);
  }

  public String getName ()
  {
    return (String) get ("FullName");
  }

  public String getNotice ()
  {
    return (String) get ("Notice");
  }

  public String getWeight ()
  {
    return (String) get ("Weight");
  }

  public byte [] [] getGlobalSubroutines ()
  {
    return fontSet.subrIndex.getSubroutines ();
  }

  public byte [] [] getSubroutines ()
  {
    return subrIndex.getSubroutines ();
  }

  public byte [] getCharString (int GID)
  {
    try {
      return (byte []) charStrings.get (GID);
    } catch (IndexOutOfBoundsException ioobe) {
      throw new InvalidGlyphException ("invalid glyph index");
    }
  }

  public byte [] getCharString (String glyph)
  {
    return getCharString (GIDfor (glyph));
  }

  public void setCharString (int GID,byte [] charstr)
  {
    General.set (charStrings,GID,charstr);
  }

  public void addCharString (byte [] charstr)
  {
    charStrings.add (charstr);
  }

  public String getGlyphName (int GID)
  {
    return glyphs != null ?
      (GID < glyphs.size () ? (String) glyphs.get (GID) : null) :
      (GID < predefinedCharsetLength () ? predefinedCharset [GID] : null);
  }

  public void setGlyphName (int GID,Object glyph)
  {
    if (glyphs == null)
      glyphs = new ArrayList (GID + 1);
    General.set (glyphs,GID,glyph);
  }

  public void addGlyphName (String glyph)
  {
    if (glyphs == null)
      glyphs = new ArrayList ();
    glyphs.add (glyph);
  }

  public void setGlyphNames (List glyphs)
  {
    this.glyphs = glyphs;
  }

  public boolean fixGlyphNames (Map unicodeToIndexMap)
  {
    boolean changed = false;
    if (glyphs != null)
      for (int i = 0;i < glyphs.size ();i++) {
        String glyph = (String) glyphs.get (i);
        if (glyph == null)
          continue;
        int unicode = GlyphList.getUnicode (glyph,true);
        if (unicode == 0)
          continue;
        Integer index = (Integer) unicodeToIndexMap.get (unicode);
        if (index == null)
          continue;
        if (index != i) {
          Options.warn ("stripping reencoded glyph " + glyph);
          glyphs.set (i,notdef);
          changed = true;
        }
      }
    return changed;
  }

  public void setWidths (Widths widths)
  {
    Assertions.expect (!isCID);
    privateDict.put ("defaultWidthX",new Double (widths.defaultWidth));
    privateDict.put ("nominalWidthX",new Double (widths.nominalWidth));
  }

  // ales79034_ch03.pdf contains a non-integer width on page 11
  private Widths getWidths (CFFDict privateDict)
  {
    return new Widths
      (((Number) privateDict.get ("defaultWidthX")).doubleValue (),
       ((Number) privateDict.get ("nominalWidthX")).doubleValue ());
  }

  public void encodeGlyph (GlyphProvider glyphProvider,String glyphName,
                           int unicode,double width,Point position)
  {
    Assertions.expect (!isCID);
    Type2CharStringEncoder encoder = new Type2CharStringEncoder (getWidths (privateDict));
    glyphProvider.interpret (encoder);
    encoder.setAdvance (width,0); // overrides intrinsic width of charstring glyph providers
    addCharString (encoder.toByteArray ());
    addGlyphName (glyphName == null ? ".notdef" : glyphName);
  }

  Type2CharStringDecoder charStringDecoder;
  Type2CharStringDecoder [] charStringDecoders;

  public Type2CharStringDecoder getCharStringDecoder ()
  {
    Assertions.expect (!isCID);

    if (charStringDecoder == null)
      charStringDecoder = new Type2CharStringDecoder
        (this,
         getSubroutines (),
         getGlobalSubroutines (),
         getWidths (privateDict));

    return charStringDecoder;
  }

  public Type2CharStringDecoder getCharStringDecoder (int i)
  {
    Assertions.expect (isCID);

    if (charStringDecoders == null)
      charStringDecoders = new Type2CharStringDecoder [selectSubrs.length];
    if (charStringDecoders [i] == null)
      charStringDecoders [i] = new Type2CharStringDecoder
        (this,
         selectSubrs [i].getSubroutines (),
         getGlobalSubroutines (),
         getWidths (privateDicts [i]));

    return charStringDecoders [i];
  }

  int fdIndex (int GID)
  {
    if (selectArray != null)
      return selectArray [GID] & 0xff;
    for (int i = 0;i < selectIndices.length;i++)
      if (selectGIDs [i] <= GID && GID < selectGIDs [i+1])
        return selectIndices [i];
    return -1;
  }

  public GlyphProvider getGlyphProvider (Object glyph)
  {
    Type2CharStringDecoder decoder;
    int GID = glyph instanceof Integer && !isCID ? ((Integer) glyph).intValue () : GIDfor (glyph);
    Transformation transform = null;
    if (isCID)
    {
      Assertions.unexpect (glyph instanceof String);
      int fdIndex = fdIndex (GID);
      if (fdIndex == -1)
        return null;
      decoder = getCharStringDecoder (fdIndex);
      transform = glyphTransforms [fdIndex];
    }
    else
      decoder = getCharStringDecoder ();
    decoder.setCharString (getCharString (GID));
    return transform == null ? decoder : new TransformedGlyphProvider (decoder,transform);
  }

  int predefinedCharsetLength ()
  {
    return predefinedCharset == standardStrings ? nISOAdobe :
      predefinedCharset.length;
  }

  SmartSoftReference glyphToIndexMap = new SmartSoftReference () {
      protected Object construct ()
      {
        Map glyphToIndexMap = new HashMap ();

        if (glyphs == null)
          for (int i = 1;i < predefinedCharsetLength ();i++)
            glyphToIndexMap.put (predefinedCharset [i],new Integer (i));
        else
          // we used to do this in ascending order, but font G5 on page 32 of
          // chron20070629A-ae.pdf contains a duplicate CID, and Adobe Reader
          // and Mac's Preview both use the lower of the two GIDs.
          for (int i = glyphs.size () - 1;i > 0;i--)
            {
              Object glyph = glyphs.get (i);
              if (glyph != null)
                if (glyphToIndexMap.put (glyph,new Integer (i)) != null)
                  Options.warn ("duplicate glyph " + glyph);
            }

        return glyphToIndexMap;
      }
    };

  public Map getGlyphToIndexMap ()
  {
    return (Map) glyphToIndexMap.get ();
  }

  public Encoding getDefaultEncoding () {
    Assertions.unexpect (isCID);
    if (encoding == null)
      return predefinedEncoding;
    Encoding properEncoding = new Encoding (fontSet.getName (this));
    for (int i = 1;i < encoding.length;i++)
      if (encoding [i] != UNENCODED)
        properEncoding.glyphs [encoding [i]] = getGlyphName (i);
    if (supplements != null)
      for (int i = 0;i < supplements.length;i++)
        if (supplements [i] != null)
          properEncoding.glyphs [i] = supplements [i];
    return properEncoding;
  }

  public void setDefaultEncoding (Encoding properEncoding) {
    Assertions.unexpect (isCID);
    encoding = new short [charStrings.size ()];
    Arrays.fill (encoding,UNENCODED);
    supplements = new String [256];
    for (int i = 0;i < charStrings.size ();i++) {
      String glyph = getGlyphName (i);
      if (glyph != null)
        for (int j = 0;j < properEncoding.glyphs.length;j++)
          if (glyph.equals (properEncoding.glyphs [j])) {
            if (encoding [i] == UNENCODED)
              encoding [i] = (byte) j;
            else
              supplements [j] = glyph;
          }
    }
  }
  
  public void putPrivate (String key,Object value)
  {
    privateDict.put (key,value);
  }

  public void subset (List indices)
  {
    if (isCID)
      throw new NotImplementedException ("subsetting CID fonts");

    CFFIndex oldCharStrings = charStrings;
    short [] oldEncoding = encoding;

    List newGlyphs = new ArrayList ();
    newGlyphs.add (notdef);
    charStrings = new CFFIndex ();
    encoding = oldEncoding == null ? null : new short [indices.size () + 1];
    
    charStrings.add (oldCharStrings.get (0));

    for (int i = 0;i < indices.size ();i++)
      {
        int GID = (Integer) indices.get (i);
        newGlyphs.add (getGlyphName (GID));
        charStrings.add (oldCharStrings.get (GID));
        if (encoding != null)
          encoding [i + 1] = oldEncoding [GID];
      }

    glyphs = newGlyphs;
    predefinedCharset = null;

    if (supplements != null)
      for (int i = 0;i < supplements.length;i++)
        if (supplements [i] != null && !glyphs.contains (supplements [i]))
          supplements [i] = null;
  }

  final static Operator [] fontOps = {
    new Operator ("FontMatrix",7 | ESCAPE),
    new Operator ("FontBBox",5),
    new Operator ("UniqueID",13),
    new Operator ("Private",18),
    new Operator ("CharStrings",17),
    new Operator ("PaintType",5 | ESCAPE),
    new Operator ("CharstringType",6 | ESCAPE),
    new Operator ("StrokeWidth",8 | ESCAPE), // Clicker-Regular in nwc.pdf
    new Operator ("Encoding",16),
    new Operator ("charset",15),
    new Operator ("XUID",14), // rev200501_4-6.pdf
    new Operator ("PostScript",21 | ESCAPE | SID),
    new Operator ("BaseFontName",22 | ESCAPE | SID),
    new Operator ("BaseFontBlend",23 | ESCAPE),
    new Operator ("version",0 | SID),
    new Operator ("Notice",1 | SID),
    new Operator ("Copyright",0 | ESCAPE | SID),
    new Operator ("FullName",2 | SID),
    new Operator ("FamilyName",3 | SID),
    new Operator ("Weight",4 | SID),
    new Operator ("isFixedPitch",1 | ESCAPE),
    new Operator ("ItalicAngle",2 | ESCAPE),
    new Operator ("UnderlinePosition",3 | ESCAPE),
    new Operator ("UnderlineThickness",4 | ESCAPE),
    // CID extensions
    new Operator ("ROS",30 | ESCAPE),
    new Operator ("CIDFontVersion",31 | ESCAPE),
    new Operator ("CIDFontRevision",32 | ESCAPE),
    new Operator ("CIDFontType",33 | ESCAPE),
    new Operator ("CIDCount",34 | ESCAPE),
    new Operator ("UIDBase",35 | ESCAPE),
    new Operator ("FDArray",36 | ESCAPE),
    new Operator ("FDSelect",37 | ESCAPE),
    // This used to be only in fontDictOps;
    // fenews200601_19.pdf has it in the main dictionary
    new Operator ("FontName",38 | ESCAPE | SID),
      };

  final static Operator [] privateOps = {
    new Operator ("BlueValues",6),
    new Operator ("OtherBlues",7),
    new Operator ("FamilyBlues",8),
    new Operator ("FamilyOtherBlues",9),
    new Operator ("BlueScale",9 | ESCAPE),
    new Operator ("BlueShift",10 | ESCAPE),
    new Operator ("BlueFuzz",11 | ESCAPE),
    new Operator ("StdHW",10),
    new Operator ("StdVW",11),
    new Operator ("StemSnapH",12 | ESCAPE),
    new Operator ("StemSnapV",13 | ESCAPE),
    new Operator ("ForceBold",14 | ESCAPE),
    // undocumented, taken from freetype
    new Operator ("ForceBoldThreshold",15 | ESCAPE),
    new Operator ("LanguageGroup",17 | ESCAPE),
    new Operator ("ExpansionFactor",18 | ESCAPE),
    new Operator ("initialRandomSeed",19 | ESCAPE),
    new Operator ("Subrs",19),
    new Operator ("defaultWidthX",20),
    new Operator ("nominalWidthX",21)
      };
  
  final static Operator [] fontDictOps = {
      new Operator ("FontName",38 | ESCAPE | SID),
      new Operator ("FontMatrix",7 | ESCAPE),
      new Operator ("CIDCount",34 | ESCAPE),
      new Operator ("Private",18)
  };
}
