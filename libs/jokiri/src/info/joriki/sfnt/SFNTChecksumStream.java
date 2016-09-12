/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.sfnt;

import java.io.OutputStream;

public class SFNTChecksumStream extends OutputStream
{
  int checksum = 0;
  int nbyte = 0;

  public void write (int b)
  {
    checksum += (b & 0xff) << ((~nbyte++ & 3) << 3);
  }

  public void write (byte [] b)
  {
    write (b,0,b.length);
  }

  public void write (byte [] b,int off,int len)
  {
    for (int i = 0;i < len;i++)
      write (b [off++]);
  }
}
