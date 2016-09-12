/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.sfnt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Map;
import java.util.HashMap;

import info.joriki.io.Util;

import info.joriki.util.Version;
import info.joriki.util.NotTestedException;
import info.joriki.util.NotImplementedException;

public class PostScriptTable extends SFNTTable
{
  public static String [] standardGlyphNames = {
    ".notdef",".null","nonmarkingreturn","space","exclam",
    "quotedbl","numbersign","dollar","percent","ampersand",
    "quotesingle","parenleft","parenright","asterisk","plus",
    "comma","hyphen","period","slash","zero",
    "one","two","three","four","five",
    "six","seven","eight","nine","colon",
    "semicolon","less","equal","greater","question",
    "at","A","B","C","D",
    "E","F","G","H","I",
    "J","K","L","M","N",
    // 50
    "O","P","Q","R","S",
    "T","U","V","W","X",
    "Y","Z","bracketleft","backslash","bracketright",
    "asciicircum","underscore","grave","a","b",
    "c","d","e","f","g",
    "h","i","j","k","l",
    "m","n","o","p","q",
    "r","s","t","u","v",
    "w","x","y","z","braceleft",
    "bar","braceright","asciitilde","Adieresis","Aring",
    // 100
    "Ccedilla","Eacute","Ntilde","Odieresis","Udieresis",
    "aacute","agrave","acircumflex","adieresis","atilde",
    "aring","ccedilla","eacute","egrave","ecircumflex",
    "edieresis","iacute","igrave","icircumflex","idieresis",
    "ntilde","oacute","ograve","ocircumflex","odieresis",
    "otilde","uacute","ugrave","ucircumflex","udieresis",
    "dagger","degree","cent","sterling","section",
    "bullet","paragraph","germandbls","registered","copyright",
    "trademark","acute","dieresis","notequal","AE",
    "Oslash","infinity","plusminus","lessequal","greaterequal",
    // 150
    "yen","mu","partialdiff","summation","product",
    "pi","integral","ordfeminine","ordmasculine","Omega",
    "ae","oslash","questiondown","exclamdown","logicalnot",
    "radical","florin","approxequal","Delta","guillemotleft",
    "guillemotright","ellipsis","nonbreakingspace","Agrave","Atilde",
    "Otilde","OE","oe","endash","emdash",
    "quotedblleft","quotedblright","quoteleft","quoteright","divide",
    "lozenge","ydieresis","Ydieresis","fraction","currency",
    "guilsinglleft","guilsinglright","fi","fl","daggerdbl",
    "periodcentered","quotesinglbase","quotedblbase","perthousand","Acircumflex",
    // 200
    "Ecircumflex","Aacute","Edieresis","Egrave","Iacute",
    "Icircumflex","Idieresis","Igrave","Oacute","Ocircumflex",
    "apple","Ograve","Uacute","Ucircumflex","Ugrave",
    "dotlessi","circumflex","tilde","macron","breve",
    "dotaccent","ring","cedilla","hungarumlaut","ogonek",
    "caron","Lslash","lslash","Scaron","scaron",
    "Zcaron","zcaron","brokenbar","Eth","eth",
    "Yacute","yacute","Thorn","thorn","minus",
    "multiply","onesuperior","twosuperior","threesuperior","onehalf",
    "onequarter","threequarters","franc","Gbreve","gbreve",
    // 250
    "Idotaccent","Scedilla","scedilla","Cacute","cacute",
    "Ccaron","ccaron","dcroat"
  };

  final static int firstCustomIndex = standardGlyphNames.length;

  Version version;
  int italicAngle;
  short underlinePosition;
  short underlineThickness;
  int isFixedPitch;
  int minMemType42;
  int maxMemType42;
  int minMemType1;
  int maxMemType1;

  String [] glyphNames;
  int [] glyphNameIndices;

  Map glyphToIndexMap = new HashMap ();
  
  public PostScriptTable (DataInput in) throws IOException
  {
    super (POST);

    version = new Version (in);
    if (version.minor != 0)
      throw new NotImplementedException ("post table version " + version);

    italicAngle = in.readInt ();
    underlinePosition = in.readShort ();
    underlineThickness = in.readShort ();
    // there are conflicting descriptions of this entry.
    // http://developer.apple.com/fonts/TTRefMan/RM06/Chap6post.html
    // specifies two bytes of isFixedPitch and two reserved zero bytes
    // http://partners.adobe.com/asn/developer/opentype/post.html and
    // http://www.microsoft.com/typography/otspec/post.htm
    // specify four bytes of isFixedPitch.
    // Since we currently don't use this information and at least
    // one file contains 0x00000001, consistent only with the latter
    // version, that's what we're using.
    isFixedPitch = in.readInt ();
    minMemType42 = in.readInt ();
    maxMemType42 = in.readInt ();
    minMemType1  = in.readInt ();
    maxMemType1  = in.readInt ();

    switch (version.major)
    {
    case 1:
      for (int i = 0;i < standardGlyphNames.length;i++)
        glyphToIndexMap.put (standardGlyphNames [i],new Integer (i));
      break;
    case 2:
      glyphNameIndices = new int [in.readUnsignedShort ()];
      int maxIndex = 0;
      for (int i = 0;i < glyphNameIndices.length;i++)
      {
        int index = in.readShort ();
        maxIndex = Math.max (maxIndex,index);
        glyphNameIndices [i] = index;
      }
      if (maxIndex >= firstCustomIndex)
      {
        glyphNames = new String [maxIndex - firstCustomIndex + 1];
        for (int i = 0;i < glyphNames.length;i++)
          glyphNames [i] = Util.readString (in);
      }
      for (int i = 0;i < glyphNameIndices.length;i++)
      {  
        int glyphNameIndex = glyphNameIndices [i];
        if (glyphNameIndex > 0)
          glyphToIndexMap.put
          (glyphNameIndex < firstCustomIndex ?
           standardGlyphNames [glyphNameIndex] :
           glyphNames [glyphNameIndex - firstCustomIndex],
           new Integer (i));
      }
      // The TrueType reference says glyphs which don't have names
      // are mapped to .notdef, e.g. 0060729937_3.pdf does this.
      glyphToIndexMap.put (".notdef",new Integer (0));
      break;
    case 3:
      break;
    default : 
      throw new NotImplementedException ("post table major version " + version.major);  
    }
  }

  public void writeTo (DataOutput out) throws IOException
  {
    version.writeTo (out);
    out.writeInt   (italicAngle);
    out.writeShort (underlinePosition);
    out.writeShort (underlineThickness);
    out.writeInt   (isFixedPitch);
    out.writeInt   (minMemType42);
    out.writeInt   (maxMemType42);
    out.writeInt   (minMemType1);
    out.writeInt   (maxMemType1);

    if (version.major == 2)
      {
        out.writeShort (glyphNameIndices.length);
        int maxIndex = 0;
        for (int i = 0;i < glyphNameIndices.length;i++)
          {
            int index = glyphNameIndices [i];
            maxIndex = Math.max (maxIndex,index);
            out.writeShort (index);
          }
        for (int i = 0;firstCustomIndex + i <= maxIndex;i++)
          {
            String glyphName = glyphNames [i];
            out.write (glyphName.length ());
            out.writeBytes (glyphName);
          }
      }
  }

  public int getGlyphIndex (String glyph)
  {
    if (version.major == 1)
      // I moved this out of the constructor since we encountered
      // a version 1 PostScript table in signbusiness200704_40.pdf
      // that was never used to get glyph indices from. 
      throw new NotTestedException ();
    Integer index = (Integer) glyphToIndexMap.get (glyph);
    return index == null ? 0 : index.intValue ();
  }
}
