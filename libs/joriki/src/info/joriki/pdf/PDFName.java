/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import info.joriki.util.General;

import info.joriki.adobe.AdobeSpeaker;

public class PDFName extends PDFBytes
{
  public PDFName ()
  {
    super ();
  }

  public PDFName (byte [] str)
  {
    super (str);
  }
  
  public PDFName (String name)
  {
    super (name);
  }
  
  public String toString ()
  {
    return externalForm (str);
  }

  public String getName ()
  {
    return getValue ();
  }

  static String externalForm (String name)
  {
    return externalForm (getBytes (name));
  }
  
  static String externalForm (byte [] str)
  {
    ByteArrayOutputStream result = new ByteArrayOutputStream ();
    try {
      write (str,result);
    } catch (IOException e) {
      throw new InternalError ();
    }
    return result.toString ();
  }
  
  static void write (String name,OutputStream out) throws IOException {
    write (getBytes (name),out);
  }

  static void write (byte [] str,OutputStream out) throws IOException {
    out.write ('/');
    for (int i = 0;i < str.length;i++)
      {
        int b = str [i]; // sign extension
        if (b > ' ' && b != '#' && b != 127 &&
            AdobeSpeaker.ctype [b] == AdobeSpeaker.REGULAR)
          out.write (b);
        else
          {
            out.write ('#');
            // this relies on the fact that toHexNybble clips with & 0xf
            out.write (General.toHexNybble (b >> 4));
            out.write (General.toHexNybble (b));
          }
      }
  }

  protected boolean write (PDFObjectWriter writer) throws IOException
  {
    write (str,writer);
    return false;
  }

  public boolean equals (Object o)
  {
    return o instanceof PDFName && super.equals (o);
  }
}
