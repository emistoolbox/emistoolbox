/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.adobe;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import info.joriki.font.FontMetrics;

import info.joriki.util.Assertions;
import info.joriki.util.NotImplementedException;

public class AFMFile implements FontMetrics
{
  String fontname;

  public Map dictionary = new HashMap ();

  String key;
  String values;

  Map glyphMap = new HashMap ();
  Glyph [] glyphTable = new Glyph [256];

  boolean isDingbats;

  void registerCode (Glyph glyph,int code)
  {
    glyph.defaultCode = code;
    if (0 <= code && code <= 255)
      glyphTable [code] = glyph;
  }

  void registerGlyph (Glyph glyph,String name)
  {
    glyph.name = name;
    glyphMap.put (name,glyph);
  }

  int bigIndex (int index)
  {
    return index == -1 ? Integer.MAX_VALUE : index;
  }

  void tokenize (String line)
  {
    int index = Math.min (bigIndex (line.indexOf (' ')),
                          bigIndex (line.indexOf ('\t')));

    if (index == Integer.MAX_VALUE)
      {
        key = line;
        values = null;
      }
    else
      {
        key = line.substring (0,index);
        values = line.substring (index + 1);
      }
  }

  public AFMFile (String filename) throws IOException
  {
    this (new File (filename));
  }

  public AFMFile (File file) throws IOException
  {
    this (new FileReader (file));
  }

  public AFMFile (InputStream in) throws IOException
  {
    this (new InputStreamReader (in));
  }

  public AFMFile (Reader in) throws IOException
  {
    BufferedReader reader = new BufferedReader (in);
    
    try {
      for (;;)
        {
          tokenize (reader.readLine ());
          if (key.equals ("FontName"))
            fontname = values;
          else if (key.equals ("StartFontMetrics"))
            Assertions.expect (values.equals ("2.0") || values.equals ("4.1"));
          else if (key.equals ("EndFontMetrics"))
            break;
          else if (key.equals ("StartCharMetrics"))
            {
              int nexpect = Integer.parseInt (values);
              int nentries = 0;
              for (;;)
                {
                  String line = reader.readLine ();
                  if (line.trim ().equals ("EndCharMetrics"))
                    break;
                  
                  StringTokenizer tok = new StringTokenizer (line,";");
                  Glyph glyph = new Glyph ();
                  while (tok.hasMoreTokens ())
                    {
                      String data = tok.nextToken ().trim ();
                      // Computer Modern Roman AFM files have trailing spaces
                      if (data.length () == 0)
                        break;
                      tokenize (data);
                      if (key.equals ("C"))
                        registerCode (glyph,Integer.parseInt (values));
                      else if (key.equals ("CH"))
                        registerCode (glyph,Integer.parseInt
                                      (values.substring
                                       (1,values.length () - 1)
                                       ,16));
                      else if (key.equals ("WX"))
                        glyph.width = Float.parseFloat (values) / 1000f;
                      else if (key.equals ("N"))
                        registerGlyph (glyph,values);
                      else if (key.equals ("L"))
                        {
                          StringTokenizer ligature = new StringTokenizer (values);
                          String next = ligature.nextToken ();
                          String result = ligature.nextToken ();
                          glyph.addLigature (next,result);
                        }
                      else if (key.equals ("B"))
                        {
                          StringTokenizer bbox = new StringTokenizer (values);
                          glyph.bbox = new double [4];
                          for (int i = 0;i < 4;i++)
                            glyph.bbox [i] =
                              Double.parseDouble (bbox.nextToken ());
                        }
                      else
                        throw new NotImplementedException ("key " + key);
                    }
                  nentries++;
                }
              Assertions.expect (nentries,nexpect);
            }
          else if (key.equals ("StartKernData"))
            {
              for (;;)
                {
                  tokenize (reader.readLine ());
                  if (key.equals ("EndKernData"))
                    break;
                  
                  if (key.equals ("StartKernPairs"))
                    {
                      int nexpect = Integer.parseInt (values);
                      int nentries = 0;
                      for (;;)
                        {
                          tokenize (reader.readLine ());
                          if (key.equals ("EndKernPairs"))
                            break;
                          
                          if (!key.equals ("KPX"))
                            throw new NotImplementedException ("kerning keyword " + key);
                          
                          StringTokenizer tok = new StringTokenizer (values);
                          
                          String first = tok.nextToken ();
                          Glyph glyph = (Glyph) glyphMap.get (first);
                          if (glyph == null)
                            throw new Error ("kerning data for undefined glyph " + first);
                          String second = tok.nextToken ();
                          double kern = Double.parseDouble (tok.nextToken ());
                          glyph.addKerning (second,kern);
                          
                          nentries++;
                        }
                      Assertions.expect (nentries,nexpect);
                    }
                  else
                    throw new NotImplementedException (key);
                }
            }
          else if (key.equals ("MetricsSets"))
            throw new NotImplementedException ("multiple metrics sets");
          else if (key.equals ("StartDirection"))
            throw new NotImplementedException ("multiple writing directions");
          else if (key.equals ("StartComposites"))
            throw new NotImplementedException ("composites");
          else if (!key.equals ("Comment"))
            {
              Assertions.unexpect (values,null);
              dictionary.put (key,values);
            }
        }
    } finally { reader.close (); }
  }

  public Encoding getDefaultEncoding ()
  {
    Encoding defaultEncoding = new Encoding ();
    Iterator glyphIterator = glyphMap.values ().iterator ();
    while (glyphIterator.hasNext ())
      {
        Glyph glyph = (Glyph) glyphIterator.next ();
        if (glyph.defaultCode != -1)
          defaultEncoding.glyphs [glyph.defaultCode] = glyph.name;
      }

    return defaultEncoding;
  }

  public float getWidth (String glyphName)
  {
    Glyph glyph = (Glyph) glyphMap.get (glyphName);
    // it seems that this is what Adobe Reader uses
    if (glyph == null)
      glyph = (Glyph) glyphMap.get ("space");
    return glyph.width;
  }

  public float getWidth (int code)
  {
    return glyphTable [code].width;
  }

  public String getGlyphName (int code)
  {
    Glyph glyph = glyphTable [code];
    return glyph == null ? null : glyph.name;
  }

  public int getAscender ()
  {
	  return getScender ("A",4);
  }

  public int getDescender ()
  {
	  return getScender ("De",2);
  }

  private int getScender (String prefix,int index)
  {
    String scender = (String) dictionary.get (prefix + "scender");
    if (scender == null)
      {
        String fontBBox = (String) dictionary.get ("FontBBox");
        StringTokenizer tok = new StringTokenizer (fontBBox," ");
        for (int i = 0;i < index;i++)
          scender = tok.nextToken ();
      }
    return Integer.parseInt (scender);
  }
}
