/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.IOException;
import java.io.OutputStream;

public class BitOutputStream implements BitSink
{
  OutputStream out;
  boolean lsbFirst;
  int bits;
  int nbits;

  public BitOutputStream (OutputStream out,boolean lsbFirst)
  {
    this.out = out;
    this.lsbFirst = lsbFirst;
  }

  public void write (boolean bit) throws IOException
  {
    int b = bit ? 1 : 0;
    if (lsbFirst)
      b <<= nbits;
    else
      bits <<= 1;
    bits |= b;
    if (++nbits == 8)
      {
        out.write (bits);
        nbits = 0;
      }
  }

  public void flush () throws IOException
  {
    while (nbits != 0)
      write (false);
  }

  public void close () throws IOException {
	  flush ();
  }

  public void writeBits (int b,int n) throws IOException {
	  b &= (1 << n) - 1;
	  if (lsbFirst)
		  b <<= nbits;
	  else
		  bits <<= n;
	  bits |= b;
	  nbits += n;
	  while (nbits >= 8) {
		  out.write (bits);
		  nbits -= 8;
	  }
  }
}
