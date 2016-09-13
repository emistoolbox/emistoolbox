/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.type1;

import java.io.IOException;
import java.io.InputStream;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Iterator;

import info.joriki.cff.CFFDict;
import info.joriki.cff.CFFFont;
import info.joriki.cff.CFFFontSet;

import info.joriki.font.Widths;

import info.joriki.util.General;
import info.joriki.util.Options;
import info.joriki.util.Assertions;
import info.joriki.util.NotImplementedException;

import info.joriki.adobe.Name;
import info.joriki.adobe.Encoding;
import info.joriki.adobe.PostScriptFile;

import info.joriki.crypto.PostScriptCypher;

import info.joriki.charstring.Type1CharStringFont;
import info.joriki.charstring.Type2CharStringEncoder;

public class Type1FontFile extends Type1CharStringFont
{
  Map fontDictionary;
  Map privateDictionary;
  Map fontInfoDictionary;
  Map charStringDictionary;

  public Type1FontFile (InputStream in) throws IOException
  {
    this (in,false);
  }

  public Type1FontFile (InputStream in,boolean hexadecimal) throws IOException
  {
    this (in,hexadecimal,0);
  }

  public Type1FontFile (InputStream in,boolean hexadecimal,int dummyBytes)
    throws IOException
  {
    fontDictionary = new PostScriptFile (in,true,hexadecimal,dummyBytes) {
        Map fontDictionary;
        protected boolean handleSpecial (String operator)
        {
          if (operator.equals ("definefont"))
            fontDictionary = (Map) operandStack.peek ();
          else if (operator.equals ("defaultmatrix"))
            // a Type 1 font file (with an incorrect Type1C
            // subtype entry) in 0765611651_3.pdf uses this
            // to test for high resolution -- we take the
            // identity transform as a reasonable guess.
            {
              List matrix = (List) operandStack.peek ();
              Assertions.expect (matrix.size (),6);
              for (int i = 0;i < 6;i++)
                matrix.set (i,new Integer (i == 0 || i == 3 ? 1 : 0));
              return true;
            }
          return false;
        }
      }.fontDictionary;
    privateDictionary    = (Map) fontDictionary.get ("Private");
    fontInfoDictionary   = (Map) fontDictionary.get ("FontInfo");
    charStringDictionary = (Map) fontDictionary.get ("CharStrings");
    
    // NEMPRINT_A3_final.pdf has a font with no FontInfo.
    // ghostscript uses an empty dictionary in this case. 
    if (fontInfoDictionary == null)
      fontInfoDictionary = new HashMap ();
  }

  public String getName ()
  {
    return ((Name) fontDictionary.get ("FontName")).name;
  }

  public String getNotice ()
  {
    byte [] notice = (byte []) fontInfoDictionary.get ("Notice");
    return notice == null ? null : new String (notice);
  }

  public String getWeight ()
  {
    byte []weight = (byte []) fontInfoDictionary.get ("Weight");
    return weight == null ? null : new String (weight);
  }

  public double [] getFontBBox ()
  {
    return General.toDoubleArray ((List) fontDictionary.get ("FontBBox"));
  }

  public double [] getFontMatrix ()
  {
    return General.toDoubleArray ((List) fontDictionary.get ("FontMatrix"));
  }

  PostScriptCypher cypher = new PostScriptCypher (PostScriptCypher.CHARSTR);

  public byte [] decrypt (byte [] str)
  {
    if (str == null)
      return null;

    cypher.reset ();
    Integer lenIV = (Integer) privateDictionary.get ("lenIV");
    int ndrop = lenIV == null ? 4 : lenIV.intValue ();
    byte [] decrypted = new byte [str.length - ndrop];
    for (int i = 0;i < str.length;i++)
      {
        byte b = cypher.decrypt (str [i]);
        if (i >= ndrop)
          decrypted [i - ndrop] = b;
      }
    return decrypted;
  }

  public byte [] getCharString (String glyph)
  {
    byte [] charString = (byte []) charStringDictionary.get (glyph);
    if (charString == null)
      {
        charString = (byte []) charStringDictionary.get (".notdef");
        if (charString == null)
          throw new Error (".notdef glyph not defined");
        Options.warn ("glyph for " + glyph + " not defined in font " + getName ());
      }
    return decrypt (charString);
  }

  public byte [] [] getSubroutines ()
  {
    List subroutineArray = (List) privateDictionary.get ("Subrs");
    byte [] [] subroutines =
      new byte [subroutineArray == null ? 0 : subroutineArray.size ()] [];
    for (int i = 0;i < subroutines.length;i++)
      subroutines [i] = decrypt ((byte []) subroutineArray.get (i));
    return subroutines;
  }

  public Encoding getDefaultEncoding ()
  {
    Object encoding = fontDictionary.get ("Encoding");
    if (encoding instanceof Name)
      {
        Name namedEncoding = (Name) encoding;
        if (namedEncoding.name.equals ("StandardEncoding"))
          return Encoding.standardEncoding;
        throw new NotImplementedException
          ("non-standard named encoding " + encoding);
      }
    else if (encoding instanceof List)
      {
        List encodingArray = (List) encoding;
        String [] glyphs = new String [encodingArray.size ()];
        for (int i = 0;i < glyphs.length;i++)
          glyphs [i] = ((Name) encodingArray.get (i)).name;
        return new Encoding (glyphs,getName ());
      }
    else
      throw new NotImplementedException ("encoding " + encoding.getClass ());
  }

  private void addDelta (CFFDict dict,Map dictionary,String key)
  {
    double [] arr = General.getDoubleArray (dictionary,key);
    if (arr != null)
      dict.putDelta (key,arr);
  }

  private void addArray (CFFDict dict,Map dictionary,String key)
  {
    double [] arr = General.getDoubleArray (dictionary,key);
    if (arr != null)
      dict.putArray (key,arr);
  }

  private void addSinglet (CFFDict dict,Map dictionary,String key)
  {
    double [] singlet = General.getDoubleArray (dictionary,key);
    if (singlet != null)
      dict.putNumber (key,singlet [0]);
  }

  private void addNumber (CFFDict dict,Map dictionary,String key)
  {
    Number number = (Number) dictionary.get (key);
    if (number != null)
      dict.putNumber (key,number.doubleValue ());
  }

  private void addBoolean (CFFDict dict,Map dictionary,String key)
  {
    Boolean bool = (Boolean) dictionary.get (key);
    if (bool != null)
      dict.putBoolean (key,bool.booleanValue ());
  }

  private void addString (CFFDict dict,Map dictionary,String key)
  {
    byte [] string = (byte []) dictionary.get (key);
    if (string != null)
      dict.putString (key,new String (string));
  }

  final static private Widths widths = new Widths (0,0);

  private void add (CFFFont font,String glyph)
  {
    font.addGlyphName (glyph);
    Type2CharStringEncoder encoder = new Type2CharStringEncoder (widths);
    charStringDecoder.decode (getCharString (glyph),encoder);
    font.addCharString (encoder.toByteArray ());
  }

  public CFFFontSet toCFFFontSet ()
  {
    CFFFontSet fontSet = new CFFFontSet ();
    CFFFont font = new CFFFont (fontSet);
  
    font.setWidths (widths);

    getCharStringDecoder ();

    add (font,".notdef");
    Iterator iterator = charStringDictionary.keySet ().iterator ();
    while (iterator.hasNext ())
      {
        String glyph = (String) iterator.next ();
        if (!glyph.equals (".notdef"))
          add (font,glyph);
      }

    CFFDict privateDict = font.privateDict;

    addDelta (privateDict,privateDictionary,"StemSnapH");
    addDelta (privateDict,privateDictionary,"StemSnapV");
    addDelta (privateDict,privateDictionary,"BlueValues");
    addDelta (privateDict,privateDictionary,"FamilyBlues");
    addDelta (privateDict,privateDictionary,"OtherBlues");
    addDelta (privateDict,privateDictionary,"FamilyOtherBlues");

    addSinglet (privateDict,privateDictionary,"StdHW");
    addSinglet (privateDict,privateDictionary,"StdVW");

    addNumber (privateDict,privateDictionary,"BlueScale");
    addNumber (privateDict,privateDictionary,"BlueFuzz");
    addNumber (privateDict,privateDictionary,"BlueShift");
    addNumber (privateDict,privateDictionary,"LanguageGroup");

    addBoolean (privateDict,privateDictionary,"ForceBold");

    addString (font,fontInfoDictionary,"version");
    addString (font,fontInfoDictionary,"Notice");
    addString (font,fontInfoDictionary,"Copyright");
    addString (font,fontInfoDictionary,"FullName");
    addString (font,fontInfoDictionary,"FamilyName");
    addString (font,fontInfoDictionary,"Weight");

    addBoolean (font,fontInfoDictionary,"isFixedPitch");
    
    addNumber (font,fontInfoDictionary,"ItalicAngle");
    addNumber (font,fontInfoDictionary,"UnderlinePosition");
    addNumber (font,fontInfoDictionary,"UnderlineThickness");
    addNumber (font,fontInfoDictionary,"StrokeWidth");
    addNumber (font,fontInfoDictionary,"UniqueID");

    addArray (font,fontDictionary,"FontMatrix");
    addArray (font,fontDictionary,"FontBBox");

    addNumber (font,fontDictionary,"PaintType");
    addNumber (font,fontDictionary,"UniqueID");

    fontSet.addFont (font,getName ());
    return fontSet;
  }
}
