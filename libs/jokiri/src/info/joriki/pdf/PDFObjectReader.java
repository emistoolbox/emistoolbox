/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

// It seems like I intended to at some point let this
// inherit from adobe.ObjectReader. This might be possible,
// but is complicated e.g. by the presence of readDictionary ().

package info.joriki.pdf;

import java.io.IOException;
import java.io.InputStream;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import info.joriki.adobe.AdobeStreamTokenizer;

public class PDFObjectReader
{
  AdobeStreamTokenizer tok;
  boolean wrapObjects;
  boolean postScriptStyle;
  boolean inlineImage = false;

  /* These are abbreviations for inline image dictionaries,
     as listed in Section 4.8.6 of the spec. The abbreviations
     for filter names are handled in PDFDictionary, since
     Acrobat allows them to be used outside inline images
     (see Implementation Note 9 in Appendix H for Section 3.3). */
  final static Map abbreviations = new HashMap ();
  static {
    abbreviations.put ("BPC","BitsPerComponent");
    abbreviations.put ("CS","ColorSpace");
    abbreviations.put ("D","Decode");
    abbreviations.put ("DP","DecodeParms");
    abbreviations.put ("F","Filter");
    abbreviations.put ("H","Height");
    abbreviations.put ("IM","ImageMask");
    abbreviations.put ("W","Width");
    abbreviations.put ("CMYK","DeviceCMYK");
    abbreviations.put ("G","DeviceGray");
    abbreviations.put ("RGB","DeviceRGB");
    abbreviations.put ("I","Indexed");
  }
  
  public PDFObjectReader (InputStream in,boolean wrapObjects)
  {
    this (in,wrapObjects,false);
  }

  public PDFObjectReader (InputStream in,boolean wrapObjects,boolean postScriptStyle)
  {
    this.wrapObjects = wrapObjects;
    this.postScriptStyle = postScriptStyle;
    setInputStream (in);
  }

  void setInputStream (InputStream in)
  {
    tok = new AdobeStreamTokenizer (in,postScriptStyle);
  }

  static final Object arrayCloser = new Object () {
      public String toString () { return "unbalanced ]"; }
    };
  static final Object dictionaryCloser = new Object () {
      public String toString () { return "unbalanced >>"; }
    };

  PDFDictionary readDictionary () throws IOException
  {
    boolean rememberNotToWrapObjects = wrapObjects;
    wrapObjects = true;
    PDFDictionary dict = new PDFDictionary ();
    Object key;
    while ((key = readObject ()) != dictionaryCloser)
      dict.put ((PDFName) key,(PDFObject) readObject ());
    dict.begin ();
    wrapObjects = rememberNotToWrapObjects;
    return dict;
  }

  PDFDictionary readInlineImageDictionary () throws IOException
  {
    inlineImage = true;
    PDFDictionary result = readDictionary ();
    inlineImage = false;
    return result;
  }

  public Object readObject () throws IOException
  {
    switch (tok.nextToken ())
      {
      case AdobeStreamTokenizer.TT_EOF :
        return null;
      case '[' :
      case '{' :
        List res = new ArrayList ();
        Object object;
        while ((object = readObject ()) != arrayCloser)
          res.add (object);
        return wrapObjects ? new PDFArray (res) : res;
      case ']' :
      case '}' :
        return arrayCloser;
      case AdobeStreamTokenizer.TT_CLOSE :
        return dictionaryCloser;
      case AdobeStreamTokenizer.TT_OPEN :
        return readDictionary ();
      case AdobeStreamTokenizer.TT_NAME :
        String fullName = inlineImage ?
          (String) abbreviations.get (new String (tok.bval)) : null;
        return fullName != null ? 
          new PDFName (fullName):
          new PDFName (tok.bval);
      case AdobeStreamTokenizer.TT_STRING :
        return wrapObjects ? new PDFString (tok.bval) : tok.bval;
      case AdobeStreamTokenizer.TT_INTEGER :
        return new PDFInteger ((int) tok.nval);
      case AdobeStreamTokenizer.TT_REAL :
        return new PDFReal (tok.nval);
      case AdobeStreamTokenizer.TT_WORD :
        String word = new String (tok.bval);
        if (word.equals ("null"))
          return PDFNull.nullObject;
        if (word.equals ("true"))
          return new PDFBoolean (true);
        if (word.equals ("false"))
          return new PDFBoolean (false);
        if (word.equals ("endobj"))
          return null;
        if (inlineImage && word.equals ("ID")) // begin image data
          return dictionaryCloser;
        return word;
      default : throw new IOException ("illegal token " + (char) tok.ttype);
      }
  }
}
