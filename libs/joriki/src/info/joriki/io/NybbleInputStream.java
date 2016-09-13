/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.IOException;
import java.io.InputStream;

import info.joriki.util.Assertions;

/**
 * A nybble input stream allows nybbles (parts of a byte)
 * to be read from the underlying input stream.
 * @see NybbleOutputStream
 */
public class NybbleInputStream extends ReadableInputStream
{
  InputStream in;
  int bits;
  int left = 0;
  int nbit;
  int mask;

  boolean lsbFirst;

  /**
   * Constructs a nybble input stream that reads half-byte nybbles
   * from the specified input stream, more significant nybble first.
   * @param in the input stream to be read from
   */
  public NybbleInputStream (InputStream in)
  {
    this (in,4,false);
  }

  /**
   * Constructs a nybble input stream that reads nybbles with the
   * specified number of bits from the specified input stream in
   * the specified order. <code>nbit</code> must be a divisor of 8.
   * @param in the input stream to be read from
   * @param nbit the number of bits to be read at a time
   * @param lsbFirst
   <table><tr><td>
   <code>true</code>
   </td><td>
   if the least significant nybble is to be read first
   </td></tr><tr><td>
   <code>false</code>
   </td><td>
   if the most significant nybble is to be read first
   </td></tr></table>
  */ 
  public NybbleInputStream (InputStream in,int nbit,boolean lsbFirst)
  {
    Assertions.expect (8 % nbit,0);

    this.in = in;
    this.nbit = nbit;
    this.mask = (1 << nbit) - 1;

    this.lsbFirst = lsbFirst;
  }

  /**
   * Reads a nybble from the underlying input stream.
   * @return the next nybble, or <code>-1</code> if the
   * end of the stream has been reached
   * @exception IOException if an I/O error occurs
   */ 
  public int read () throws IOException
  {
    if (left == 0)
      {
        bits = in.read ();
        if (bits == -1)
          return -1;
        left = 8;
      }

    left -= nbit;
    int result = bits;

    if (lsbFirst)
      bits >>= nbit;
    else
      result >>= left;

    return result & mask;
  }

  /**
   * Flushes any buffered nybbles to restart on a byte boundary.
   */
  public void flush ()
  {
    left = 0;
  }
}
