/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.compression.huffman;

import java.io.IOException;

import info.joriki.io.PeekableBitSource;
import info.joriki.io.ReadableInputStream;

import info.joriki.io.filter.InputFilter;

import info.joriki.util.Handler;
import info.joriki.util.Assertions;

public class HuffmanDecoder extends ReadableInputStream implements InputFilter 
{
  int maxlen;
  HuffmanLeaf [] lookup;

  public HuffmanDecoder (HuffmanCode code)
  {
    maxlen = code.maxlen;
    lookup = new HuffmanLeaf [1 << maxlen];
    code.traverse (new Handler<HuffmanLeaf> () {
        public void handle (HuffmanLeaf leaf)
        {
          int shift = maxlen - leaf.codeLength;
          int beg = leaf.code << shift;
          int end = (leaf.code + 1) << shift;
          for (int i = beg;i < end;i++)
            {
              Assertions.expect (lookup [i],null);
              lookup [i] = leaf;
            }
        }
      });
  }
  
  PeekableBitSource in;

  /**
     Sets the source that this decoder reads from.
     The source must implement the <code>PeekableBitSource</code> interface.
     @param source the source to be read from
  */
  public void setSource (Object source)
  {
    in = (PeekableBitSource) source;
  }

  /**
     Returns the source provided by this decoder, namely the decoder itself.
     @return the source provided by this decoder
  */
  public Object getSource () { return this; }

  /**
   * Reads the next Huffman code and returns the corresponding symbol.
   * @return
   <table><tr valign=top><td>
   the next decoded symbol
   </td><td>
   if a code was successfully processed
   </td></tr><tr valign=top><td>
   {@link io.filter.Filter#EOI EOI}
   </td><td>
   if the decoder encountered <code>EOI</code><BR>
   </td></tr><tr valign=top><td>
   {@link io.filter.Filter#EOD EOD}
   </td><td>
   if the converter encountered <code>EOD</code> and needs
   more bits to proceed
   </td></tr></table>
   * @exception IOException if an I/O error occurs
   */
  public int read () throws IOException
  {
    int nextBits = in.peekBits (maxlen);

    if (nextBits == EOD)
      return EOD;
    if (nextBits == EOI)
      {
        int npeek = maxlen;
        while ((nextBits = in.peekBits (--npeek)) == EOI)
          ;
        if (npeek == 0)
          return EOI;
        nextBits <<= maxlen - npeek;
      }

    HuffmanLeaf leaf = lookup [nextBits];

    try {
      in.dropBits (leaf.codeLength);
      return leaf.symbol;
    } catch (NullPointerException npe) {
      throw new UndefinedHuffmanCodeException (nextBits);
    }
  }
}
