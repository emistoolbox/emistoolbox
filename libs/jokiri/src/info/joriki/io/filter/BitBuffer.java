/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io.filter;

import info.joriki.io.PeekableBitSource;
import info.joriki.io.BitSink;

import java.io.IOException;

/**
   A bit buffer buffers bits. It acts both as a bit source and as
   a bit sink. For efficiency, the current implementation only
   buffers up to 32 bits.
*/
public class BitBuffer extends AtomicBuffer implements PeekableBitSource,BitSink
{
  boolean flushWithOnes;
  boolean lsbFirst;
  /**
   * Bits written and not read
   */
  private int bits;

  /**
     Constructs a new bit buffer with the specified bit ordering
     and a policy of flushing with zeros.
     @param lsbFirst
     <table><tr><td>
     <code>true</code>
     </td><td>
     if the least significant bit written is to be read first
     </td></tr><tr><td>
     <code>false</code>
     </td><td>
     if the most significant bit written is to be read first
     </td></tr></table>
     @see #flush
  */
  public BitBuffer (boolean lsbFirst)
  {
    this (lsbFirst,false);
  }

  /**
     Constructs a new bit buffer with the specified bit ordering
     and the specified flushing policy.
     @param lsbFirst
     <table><tr><td>
     <code>true</code>
     </td><td>
     if the least significant bit written is to be read first
     </td></tr><tr><td>
     <code>false</code>
     </td><td>
     if the most significant bit written is to be read first
     </td></tr></table>
     @param flushWithOnes
     <table><tr><td>
     <code>true</code>
     </td><td>
     if the last byte is to be filled with |s upon flushing
     </td></tr><tr><td>
     <code>false</code>
     </td><td>
     if the last byte is to be filled with Os upon flushing
     </td></tr></table>
     @see #flush
  */
  public BitBuffer (boolean lsbFirst,boolean flushWithOnes)
  {
    this.lsbFirst = lsbFirst;
    this.flushWithOnes = flushWithOnes;
  }

  final private int mask (int n)
  {
    return (1 << n) - 1;
  }

  /**
     Reads up to 31 bits, cranking the input crank if necessary.
     @param n the number of bits to be read
     @return
     <table><tr valign=top><td>
     <code>n</code> bits
     </td><td>
     if they were available or could be provided by the input crank
     </td></tr><tr valign=top><td>
     {@link Filter#EOI EOI}
     </td><td>
     if the input crank was not able to provide the requested bits
     </td></tr><tr valign=top><td>
     {@link Filter#EOD EOD}
     </td><td>
     if the requested bits are not available and
     this buffer has no input crank to crank
     </td></tr></table>
     @exception IOException if the input crank throws an
     <code>IOException</code>
  */
  public int readBits (int n) throws IOException
  {
    int eod = inputCrank (n);
    if (eod != OK)
      return eod;
    len -= n;
    int result = bits;
    if (lsbFirst)
      bits >>= n;
    else
      result >>= len;
    return result & mask (n);
  }

  /**
     Peeks at up to 31 bits without consuming them,
     cranking the input crank if necessary.
     @param n the number of bits to be peeked at
     @return
     <table><tr valign=top><td>
     <code>n</code> bits
     </td><td>
     if they were available or could be provided by the input crank
     </td></tr><tr valign=top><td>
     {@link Filter#EOI EOI}
     </td><td>
     if the input crank was not able to provide the requested bits
     </td></tr><tr valign=top><td>
     {@link Filter#EOD EOD}
     </td><td>
     if the requested bits are not available and
     this buffer has no input crank to crank
     </td></tr></table>
     @exception IOException if the input crank throws an
     <code>IOException</code>
  */
  public int peekBits (int n) throws IOException
  {
    int eod = inputCrank (n);
    if (eod != OK)
      return eod;
    return (lsbFirst ? bits : bits >> (len - n)) & mask (n);
  }

  /**
   * Drops the specified number of bits from this bit buffer. This
   * method does not try to obtain new data; the caller must know
   * that the buffer actually contains <code>n</code> bits,
   * typically due to a previous call to {@link #peekBits}.
   * Use {@link #readBits} if you want to drop bits that are
   * not yet in the buffer.
   * @param n the number of bits to be dropped
   * @exception IllegalArgumentException if the buffer contains less than <code>n</code> bits
   */
  public void dropBits (int n)
  {
    if (len < n)
      throw new IllegalArgumentException ("cannot drop " + n + " bits from " + len);
    if (lsbFirst)
      bits >>= n;
    len -= n;
  }

  /**
     Writes up to 32 bits to the buffer and then cranks the
     output crank if there is one.
     @param b the bits to be written
     @param n the number of bits to be written
     @exception IOException if the output crank throws an
     <code>IOException</code> or the buffer's capacity of
     32 bits is exceeded.
  */
  public void writeBits (int b,int n) throws IOException
  {
    b &= mask (n);
    if (lsbFirst)
      b <<= len;
    else
      bits <<= n;
    bits |= b;
    if ((len += n) > 32)
      throw new IOException ("bit overflow");
    outputCrank ();
  }

  /**
     Flushes the bit buffer. The <code>flush</code> method
     of <code>BitBuffer</code> writes the minimal number
     of bits such that an integral number of bytes
     remains to be read. Whether Os or |s are written is
     determined by the flushing policy specified to the constructor.
     @see #BitBuffer(boolean)
     @see #BitBuffer(boolean,boolean)
     @exception IOException if an I/O error occured
  */
  public void flush () throws IOException
  {
    writeBits (flushWithOnes ? -1 : 0,8 - (((len - 1) & 7) + 1));
  }

  /**
   * Tests whether the bits remaining in the buffer are all
   * zeros or ones, depending on the flushing policy.
   @return true if all bits are flush bits, false otherwise
  */
  public boolean isFlushed ()
  {
    int mask = mask (len);
    return (bits & mask) == (flushWithOnes ? mask : 0);
  }

  public void byteAlign ()
  {
    dropBits (len & 7);
  }
}
