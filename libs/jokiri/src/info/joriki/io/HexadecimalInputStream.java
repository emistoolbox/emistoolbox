/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;

import info.joriki.util.General;

public class HexadecimalInputStream extends ReadableInputStream
{
  InputStream in;

  public HexadecimalInputStream (InputStream in)
  {
    this.in = in;
  }

  private final void checkHexDigit (int c) throws StreamCorruptedException
  {
    if (!General.isHexDigit (c))
      throw new StreamCorruptedException
        ("expected hexadecimal digit, found " + (char) c);
  }

  public int read () throws IOException
  {
    int c;
    while (General.isWhiteSpace (c = in.read ()))
      ;
    if (c < 0)
      return c;
    checkHexDigit (c);
    int d = in.read ();
    if (d < 0)
      return d;
    checkHexDigit (d);
    return General.hexByte (c,d);
  }
}
