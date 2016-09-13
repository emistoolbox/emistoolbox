/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io.filter;

import info.joriki.io.BitSourceInputStream;
import info.joriki.io.BitSinkOutputStream;

/**
 * This class contains convenience methods for bit stream coders
 * which hide the bit-level I/O by constructing a byte-oriented filter with
 * a bit stream coder at its core.
 */
public class BitStreamCoders
{
  private BitStreamCoders () {}

  /**
   * Constructs a byte-oriented input filter with the specified bit ordering
   * from the specified bit stream encoder.
   * @param bse the bit stream encoder to be wrapped
   * @param lsbFirst
   <table><tr><td>
   <code>true</code>
   </td><td>
   if the first encoded bits are to become the least significant bits of the bytes produced
   </td></tr><tr><td>
   <code>false</code>
   </td><td>
   if the first encoded bits are to become the most significant bits of the bytes produced
   </td></tr></table>
   * @return a byte-oriented input filter with the specified bit stream
   * encoder at its core
   * @see BitBuffer
   */
  public static InputFilter getByteInputFilter (BitStreamEncoder bse,boolean lsbFirst)
  {
    return
      Concatenator.concatenate
      (Concatenator.concatenate
       (Concatenator.concatenate
        (new BytePusher (),bse),
        new BitBuffer (lsbFirst)),
       new BitSourceInputStream ());
  }
  
  /**
   * Constructs a byte-oriented output filter with the specified bit ordering
   * from the specified bit stream encoder.
   * @param bse the bit stream encoder to be wrapped
   * @param lsbFirst
   <table><tr><td>
   <code>true</code>
   </td><td>
   if the first encoded bits are to become the least significant bits of the bytes produced
   </td></tr><tr><td>
   <code>false</code>
   </td><td>
   if the first encoded bits are to become the most significant bits of the bytes produced
   </td></tr></table>
   * @return a byte-oriented output filter with the specified bit stream
   * encoder at its core
   * @see BitBuffer
   */
  public static OutputFilter getByteOutputFilter (BitStreamEncoder bse,boolean lsbFirst)
  {
    return
      Concatenator.concatenate
      (Concatenator.concatenate
       (bse,new BitBuffer (lsbFirst)),
       new BitsToBytesConverter ());
  }

  /**
   * Constructs a byte-oriented input filter with the specified bit ordering
   * from the specified bit stream decoder.
   * @param bsd the bit stream decoder to be wrapped
   * @param lsbFirst
   <table><tr><td>
   <code>true</code>
   </td><td>
   if the least significant bits of the source bytes are to be decoded first
   </td></tr><tr><td>
   <code>false</code>
   </td><td>
   if the most significant bits of the source bytes are to be decoded first
   </td></tr></table>
   * @return a byte-oriented input filter with the specified bit stream
   * decoder at its core
   * @see BitBuffer
   */
  public static InputFilter getByteInputFilter (BitStreamDecoder bsd,boolean lsbFirst)
  {
    return
      Concatenator.concatenate
      (Concatenator.concatenate
       (Concatenator.concatenate
        (new BytesToBitsConverter (),new BitBuffer (lsbFirst)),
        bsd),
       new ByteBuffer ());
  }

  /**
   * Constructs a byte-oriented output filter with the specified bit ordering
   * from the specified bit stream decoder.
   * @param bsd the bit stream decoder to be wrapped
   * @param lsbFirst
   <table><tr><td>
   <code>true</code>
   </td><td>
   if the least significant bits of the source bytes are to be decoded first
   </td></tr><tr><td>
   <code>false</code>
   </td><td>
   if the most significant bits of the source bytes are to be decoded first
   </td></tr></table>
   * @return a byte-oriented output filter with the specified bit stream
   * decoder at its core
   * @see BitBuffer
   */
  public static OutputFilter getByteOutputFilter (BitStreamDecoder bsd,boolean lsbFirst)
  {
    return
      Concatenator.concatenate
      (Concatenator.concatenate
       (new BitSinkOutputStream (),new BitBuffer (lsbFirst)),
       bsd);
  }
}
