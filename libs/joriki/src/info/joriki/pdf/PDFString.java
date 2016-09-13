/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import info.joriki.util.Options;

import info.joriki.adobe.Encoding;
import info.joriki.crypto.StreamCypher;
import info.joriki.io.SaneCharArrayWriter;

public class PDFString extends PDFBytes
{
  public PDFString ()
  {
    super ();
  }

  public PDFString (byte [] str)
  {
    super (str);
  }

  public PDFString (String str)
  {
    super (str);
  }

  public String toString ()
  {
    ByteArrayOutputStream baos = new ByteArrayOutputStream ();
    try {
      writeTo (baos,null);
    } catch (IOException e) {
      throw new InternalError ();
    }
    return baos.toString ();
  }

  public boolean equals (Object o)
  {
    return o instanceof PDFString && super.equals (o);
  }

  public String toAsciiString ()
  {
    return getValue ();
  }

  public String toTextString ()
  {
    return toTextString (false);
  }

  public String toTextString (boolean allowEOL)
  {
    byte [] str = getBytes ();
    SaneCharArrayWriter writer = new SaneCharArrayWriter ();

    if (str.length >= 2 && str [0] == -2 && str [1] == -1)
    {
      // This string is UTF-16 encoded.
      // For our present purposes, this means that
      // two consecutive bytes form one Unicode.
      if ((str.length & 1) != 0) // Codigo_penal_act.pdf
        Options.warn ("UTF-16 string contains odd number of bytes");
      
      for (int i = 2;i + 1 < str.length;)
      {
        int b1 = str [i++] & 0xff;
        int b2 = str [i++] & 0xff;
        if (b1 == 0 && b2 == 27) // skip language escape code
        {
          while (str [i++] != 0 | str [i++] != 27) // non-abortive |
            ;
          continue;
        }
        writer.write ((b1 << 8) | b2);
      }
    }
    else
      for (int i = 0;i < str.length;i++)
        {
          int c = str [i] & 0xff;
          // The allowEOL option is for retrieving Contents.
          // '\n' isn't actually allowed, but some of the
          // Texterity files have \r unescaped in the Contents
          // string; this is converted to \n during parsing
          // (according to the spec), so we have to catch
          // that case here, too.
          writer.write (allowEOL && (c == '\r' || c == '\n') ? '\n' :
                        Encoding.PDFDocEncoding.getUnicode (c));
        }

    return writer.toString ();
  }

	private boolean writeHex = false;

	public void setWriteHex (boolean writeHex) {
		this.writeHex = writeHex;
	}

  public boolean write (PDFObjectWriter writer) throws IOException {
    writeTo (writer,writer.crypt.getCypher ());
    return true;
  }

  private void writeTo (OutputStream out,StreamCypher cypher) throws IOException {
	  if (writeHex) {
		    out.write ('<');
		    for (byte b : str) {
		      if (cypher != null)
		        b = cypher.encrypt (b);
		      out.write (nibble (b >> 4));
		      out.write (nibble (b));
		    }
		    out.write ('>');
	  }
	  else {
		    out.write ('(');
		    for (byte b : str) {
		      if (cypher != null)
		        b = cypher.encrypt (b);
		      switch (b) {
		      case '\r' :
		        b = 'r';
		        // fall through
		      case '(' :
		      case ')' :
		      case '\\' :
		        out.write ('\\');
		        // fall through
		      default :
		        out.write (b);
		      }
		    }
		    out.write (')');
	  }
  }
  
  private static int nibble (int b) {
	  b &= 0xf;
	  return 0 <= b && b <= 9 ? '0' + b : 'A' + (b - 10);
  }
}
