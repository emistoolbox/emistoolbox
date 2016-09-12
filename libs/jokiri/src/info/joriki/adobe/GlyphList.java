/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.adobe;

import java.io.IOException;

import java.util.Map;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import info.joriki.io.ParseReader;
import info.joriki.io.Resources;
import info.joriki.io.SaneCharArrayWriter;

import info.joriki.util.Assertions;
import info.joriki.util.General;
import info.joriki.util.NotTestedException;
import info.joriki.util.Unicode;

public class GlyphList
{
  public final static GlyphList glyphList = new GlyphList ("glyphlist.txt");
  final static GlyphList dingbatsList = new GlyphList ("zapfdingbats.txt");

  final Map glyphNameToUnicodesMap = new HashMap ();
  final Map unicodeToGlyphNameMap = new HashMap ();
  
  private GlyphList (String filename)
  {
    try {
      ParseReader reader = new ParseReader (Resources.getReader (GlyphList.class,filename),filename);
      try {
        String line;
        while ((line = reader.readLine ()) != null)
          if (line.length () != 0 && line.charAt (0) != '#')
          {
            StringTokenizer tok = new StringTokenizer (line,";");
            String glyph = tok.nextToken ();
            String codes = tok.nextToken ();
            Assertions.unexpect (tok.hasMoreTokens());
            Assertions.expect (codes.length () % 5,4);
            char [] unicodes = new char [(codes.length () + 1) / 5];
            for (int i = 0,index = 0;i < unicodes.length;i++,index += 5)
            {
              if (i != 0)
                Assertions.expect (codes.charAt (index - 1),' ');
              int code = parseHexadecimal(codes.substring (index,index + 4));
              Assertions.unexpect (code,-1);
              unicodes [i] = (char) code;
            }
            glyphNameToUnicodesMap.put (glyph,unicodes);
            if (unicodes.length == 1)
            {
              Character unicode = new Character (unicodes [0]);
              String previous = (String) unicodeToGlyphNameMap.put (unicode,glyph);
              // In all cases, if one of the conflicting glyph names occurs in
              // one or more of the predefined encodings, the other doesn't.
              // We prefer the former. Since the lists are in alphabetical order,
              // we need to reinstate the preferred glyph name if it came first.  
              // In most cases, the preferred glyph name occurs in the Symbol
              // encoding; in the three remaining cases, it occurs in most of
              // the others; we arbitrarily use the standard encoding.
              if (previous != null &&
                  (Encoding.  symbolEncoding.getGlyphToCodeMap().containsKey (previous) ||
                   Encoding.standardEncoding.getGlyphToCodeMap().containsKey (previous)))
                unicodeToGlyphNameMap.put (unicode,previous);
            }
          }
        } catch (NoSuchElementException nsee) {
        reader.throwException ("Too few entries");
      } catch (NumberFormatException nfe) {
        reader.throwException ("expected Unicode");
      } finally {
        reader.close ();
      }
    } catch (IOException ioe) {
      System.err.println ("couldn't read glyph list from " + filename);
    }
  }

  private char [] getUnicodes (String glyph)
  {
    char [] unicodes = (char []) glyphNameToUnicodesMap.get (glyph);
    if (unicodes != null && unicodes.length != 1)
      throw new NotTestedException ();
    return unicodes;
  }

  public char getUnicode (String glyph) {
    char [] unicodes = getUnicodes (glyph);
    Assertions.expect (unicodes.length,1);
    return unicodes [0];
  }

  static public int getUnicode (String glyph,boolean isDingbats)
  {
    char [] unicodes = getUnicodes (glyph,isDingbats);
    return
      unicodes.length == 1 ? unicodes [0] :
      unicodes.length == 2 && Unicode.isSurrogatePair (unicodes [0],unicodes [1]) ?
                              Unicode.toCodePoint     (unicodes [0],unicodes [1]) :
      0;
  }

  static int parseHexadecimal (String hexadecimal)
  {
    for (int i = 0;i < hexadecimal.length ();i++)
      if (!General.isUppercaseHexDigit(hexadecimal.charAt (i)))
        return -1;
    int result = Integer.parseInt (hexadecimal,16);
    return 0xd800 <= result && result < 0xe000 ? -1 : result;
  }
  
  // algorithm at http://partners.adobe.com/asn/tech/type/unicodegn.jsp
  static public char [] getUnicodes (String glyph,boolean isDingbats)
  {
    int period = glyph.indexOf ('.');
    if (period != -1)
      glyph = glyph.substring(0,period);

    SaneCharArrayWriter writer = new SaneCharArrayWriter (1);
    StringTokenizer tok = new StringTokenizer (glyph,"_");
    while (tok.hasMoreTokens())
    {
      glyph = tok.nextToken ();
      char [] unicodes;
      if ((isDingbats &&
          (unicodes = dingbatsList.getUnicodes (glyph)) != null) ||
          (unicodes =    glyphList.getUnicodes (glyph)) != null)
        writer.write (unicodes);
      else if (glyph.startsWith("uni") && (glyph.length () & 3) == 3)
      {
        char [] parts = new char [glyph.length () >> 2];
        int j = 0;
        for (int i = 3;j < parts.length;i += 4)
        {
          int value = parseHexadecimal (glyph.substring (i,i + 4));
          if (value == -1)
            break;
          parts [j++] = (char) value;
        }
        if (j == parts.length)
          writer.write (parts);
      }
      else if (glyph.startsWith ("u") && 5 <= glyph.length () && glyph.length () <= 7)
      {
        int value = parseHexadecimal (glyph.substring (1));
        if (value != -1)
          writer.write (value);
      }
    }
    return writer.toCharArray();
  }

  private String mapGlyphName (int unicode)
  {
    // there are no "big" unicodes in the glyph list
    return unicode >= 0x10000 ? null : (String) unicodeToGlyphNameMap.get (new Character ((char) unicode));
  }

  static public String getGlyphName (int unicode)
  {
    // There are some conflicts between unicodes in the two lists.
    // In all cases, if one of the names occurs in one of the
    // predefined encodings, it's the one from the main glyph list.
    String glyph = glyphList.mapGlyphName (unicode);
    if (glyph == null)
      glyph = dingbatsList.mapGlyphName (unicode);
    return glyph == null ? "uni" + General.zeroPad (Integer.toHexString (unicode).toUpperCase (),4) : glyph;
  }
}
