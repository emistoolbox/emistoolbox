/*
 * Copyright 2005 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.compression.ascii;

import java.io.IOException;
import java.io.StreamCorruptedException;

public class Base16Decoder extends BaseDecoder
{
  public Base16Decoder ()
  {
    super (16,2,1);
  }

  protected int addAscii (int ascii) throws IOException
  {
    if ('0' <= ascii && ascii <= '9')
      addDigit (ascii - '0');
    else if ('A' <= ascii && ascii <= 'F')
      addDigit (ascii - 'A' + 10);
    else if ('a' <= ascii && ascii <= 'f')
      addDigit (ascii - 'a' + 10);
    else if (ascii == '>') {
      if (nascii == 1)
        addDigit (0);
      return EOI;
    }
    else
      throw new StreamCorruptedException ();
    return OK;
  }
}
