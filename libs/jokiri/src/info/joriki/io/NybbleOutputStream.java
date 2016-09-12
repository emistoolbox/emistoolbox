/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.IOException;
import java.io.OutputStream;

import info.joriki.util.Assertions;

/**
 * A nybble output stream allows nybbles (parts of a byte)
 * to be written to the underlying output stream.
 * @see NybbleInputStream
 */
public class NybbleOutputStream extends WriteableOutputStream
{
  OutputStream out;
  int bits;
  int left = 0;
  int nbit;
  int mask;

  boolean lsbFirst;

  /**
   * Constructs a nybble output stream that writes half-byte nybbles
   * to the specified output stream, more significant nybble first.
   * @param out the output stream to be written to
   */
  public NybbleOutputStream (OutputStream out)
  {
    this (out,4,false);
  }

  /**
   * Constructs a nybble output stream that writes nybbles with the
   * specified number of bits to the specified output stream in
   * the specified order. <code>nbit</code> must be a divisor of 8.
   * @param out the output stream to be written to
   * @param nbit the number of bits to be written at a time
   * @param lsbFirst
   <table><tr><td>
   <code>true</code>
   </td><td>
   if the least significant nybble is to be written first
   </td></tr><tr><td>
   <code>false</code>
   </td><td>
   if the most significant nybble is to be written first
   </td></tr></table>
  */ 
  public NybbleOutputStream (OutputStream out,int nbit,boolean lsbFirst)
  {
    Assertions.expect (8 % nbit,0);

    this.out = out;
    this.nbit = nbit;
    this.mask = (1 << nbit) - 1;

    this.lsbFirst = lsbFirst;
  }

  /**
   * Writes a nybble to the underlying output stream.
   * @param b the nybble to be written
   * @exception IOException if an I/O error occurs
   */
  public void write (int b) throws IOException
  {
    b &= mask;
    if (lsbFirst)
      b <<= left;
    else
      bits <<= nbit;
    bits |= b;
    if ((left += nbit) == 8)
      {
        out.write (bits);
        left = 0;
      }
  }

  /**
   * Closes the nybble output stream. The <code>close</code> method
   * of <code>NybbleOutputStream</code> simply calls the {@link #flush}
   * method.
   * @exception IOException if an I/O error occurs
   */
  public void close () throws IOException
  {
    flush ();
  }

  /**
   * Flushes any buffered nybbles, padding the remainder of a byte
   * with zero nybbles and restarting on a byte boundary.
   * @exception IOException if an I/O error occurs
   */
  public void flush () throws IOException
  {
    flush (0);
  }

  /**
   * Flushes any buffered nybbles, padding the remainder of a byte
   * with the specified nybble and restarting on a byte boundary.
   * @param pad the nybble to be used for padding
   * @exception IOException if an I/O error occurs
   */
  public void flush (int pad) throws IOException
  {
    while (left != 0)
      write (pad);
  }
}
