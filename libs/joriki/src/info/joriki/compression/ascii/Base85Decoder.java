/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.compression.ascii;

import java.io.IOException;
import java.io.StreamCorruptedException;

public class Base85Decoder extends BaseDecoder
{
  public Base85Decoder ()
  {
    super (85,5,4);
  }

  protected int addAscii (int ascii) throws IOException
  {
    if ('!' <= ascii && ascii <= 'u') // normal case
      addDigit (ascii - '!');
    else
      switch (ascii)
        {
        case 'z' :
          if (nascii != 0)
            throw new StreamCorruptedException ();
          for (int i = 0;i < nout;i++)
            out.write (0);
          break;
        case '~' :
          if (in.read () != '>')
            throw new StreamCorruptedException ();
          return close ();
        default :
          throw new StreamCorruptedException ();
        }

    return OK;
  }
}
