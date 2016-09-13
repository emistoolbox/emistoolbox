/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.io.UnsupportedEncodingException;

import java.util.Arrays;

import info.joriki.util.General;

class PDFBytes extends PDFValue implements Comparable
{
  // This is the only charset guaranteed to be defined
  // that gives us back exactly the bytes we put in. 
  final static String charset = "ISO-8859-1";

  byte [] str;

  PDFBytes ()
  {
    str = new byte [0];
  }

  PDFBytes (byte [] str)
  {
    this.str = str;
  }

  PDFBytes (String str)
  {
    this.str = getBytes (str);
  }

  public boolean equals (Object o)
  {
    return o instanceof PDFBytes && Arrays.equals (str,((PDFBytes) o).str);
  }

  public int compareTo (Object o)
  {
    return General.compare (str,((PDFBytes) o).str);
  }

  protected void setValue (String value)
  {
    str = getBytes (value);
  }

  static protected byte [] getBytes (String value)
  {
    try {
      return value.getBytes (charset);
    } catch (UnsupportedEncodingException uee) { throw new InternalError (); }
  }

  protected String getValue ()
  {
    try {
      return new String (str,charset);
    } catch (UnsupportedEncodingException uee) { throw new InternalError (); }
  }

  public String getUTFString ()
  {
    try {
      return new String (str,"UTF-8");
    } catch (UnsupportedEncodingException uee) { throw new InternalError (); }
  }

  public byte [] getBytes ()
  {
    return str;
  }

  public int hashCode ()
  {
    int res = 0;
    for (int i = 0;i < str.length;i++)
      {
        res *= 27;
        res += str [i];
      }
    return res;
  }
}
