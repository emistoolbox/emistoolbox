/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.compression.huffman;

import java.io.IOException;

import info.joriki.io.BitSink;
import info.joriki.io.WriteableOutputStream;

import info.joriki.io.filter.OutputFilter;

import info.joriki.util.Handler;
import info.joriki.util.Count;

public class HuffmanEncoder extends WriteableOutputStream implements OutputFilter
{
  BitSink sink;
  HuffmanLeaf [] leaves;

  public HuffmanEncoder (HuffmanCode code)
  {
    final Count count = new Count ();
    code.traverse (new Handler<HuffmanLeaf> () {
        public void handle (HuffmanLeaf leaf)
        {
          count.count = Math.max (leaf.symbol,count.count);
        }
      });
    leaves = new HuffmanLeaf [count.count + 1];
    code.traverse (new Handler<HuffmanLeaf> () {
        public void handle (HuffmanLeaf leaf)
        {
          leaves [leaf.symbol] = leaf;
        }
      });
  }

  /**
     Sets the sink that this decoder writes to.
     The sink must implement the <code>BitSink</code> interface.
     @param sink the sink to be written to
  */
  public void setSink (Object sink)
  {
    this.sink = (BitSink) sink;
  }

  /**
     Returns the sink provided by this encoder, namely the encoder itself.
     @return the sink provided by this encoder
  */
  public Object getSink () { return this; }

  public void write (int b) throws IOException
  {
    HuffmanLeaf leaf = leaves [b & 0xff];
    try {
      sink.writeBits (leaf.code,leaf.codeLength);
    } catch (NullPointerException e) {
      e.printStackTrace ();
      System.err.println (this + " : " + Integer.toHexString (b) + " " + sink + " " + leaf);
      throw e;
    }
  }
}
