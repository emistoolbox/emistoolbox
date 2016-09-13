/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.compression.lzw;

import java.util.Vector;

import java.io.IOException;
import java.io.StreamCorruptedException;

import info.joriki.io.filter.BitStreamDecoder;

import info.joriki.io.BitSource;
import info.joriki.io.ByteSink;

/**
 * An LZWDecoder performs LZW decompression. References for the LZW compression scheme are provided in the <a href="package-summary.html#package_description">package description</a>.
 * @version 1.0, June 5 2001
 * @author Felix Pahl
 * @see     lzw.LZWEncoder
 */

public class LZWDecoder extends LZWCoder implements BitStreamDecoder
{
  /**
   * Constructs a new LZW decoder with the specified parameters.
   * These parameters are described in
   * <a href="package-summary.html#package_description">package description</a>.
   */
  public LZWDecoder (int rootSize,int earlyChange)
  {
    super (rootSize,earlyChange);

    // initialize the code tree and code table
    for (int i = 0;i < nroot;i++)
      {
        // a root, which stands for a single "letter"
        Entry root = new Entry ();
        root.lett = (byte) i;
        root.dad = null;
        // add it as the i-th element in the table
        table.addElement (root);
      }

    reset ();
  }

  // a node in the code tree, an entry in the code table
  class Entry
  {
    // the last "letter" of the string this entry stands for
    byte lett;
    // the ancestor node, leading to the other letters
    Entry dad;

    // output the letters of the string in order, return the first
    byte recurse () throws IOException
    {
      byte result = lett;
      if (dad != null)
        result = dad.recurse ();
      out.write (lett);
      return result;
    }
  }

  BitSource in;
  ByteSink out;

  /**
     Sets the source that this decoder reads from.
     The source must implement the <code>BitSource</code> interface.
     @param source the source to be read from
  */
  public void setSource (Object source)
  {
    in = (BitSource) source;
  }

  /**
     Sets the sink that this decoder writes to.
     The sink must implement the <code>ByteSink</code> interface.
     @param sink the sink to be written to
  */
  public void setSink (Object sink)
  {
    out = (ByteSink) sink;
  }

  Vector table = new Vector ();
  Entry prev;
  boolean cleared;
  boolean closed;

  void clearTable ()
  {
    clearCodes ();
    table.setSize (nextCode);
  }

  /**
   * Resets the decoder. This method must be called when the decoder
   * is to be reused after processing data.
   */
  public void reset ()
  {
    clearTable ();
    prev = null;
    cleared = true;
    closed = false;
  }

  /**
   * Processes one code. This method reads a number of bits as determined
   * by the current code size and, unless the code was one of the special
   * codes, writes the corresponding decompressed data to the sink.
   * @return
     <table><tr valign=top><td>
     {@link io.filter.Filter#OK OK}
     </td><td>
     if a code was successfully processed
     </td></tr><tr valign=top><td>
     {@link io.filter.Filter#EOI EOI}
     </td><td>
     if the decoder encountered <code>EOI</code> or the LZW end code<BR>
     </td></tr><tr valign=top><td>
     {@link io.filter.Filter#EOD EOD}
     </td><td>
     if the decoder encountered <code>EOD</code> and needs
     more bits to proceed
     </td></tr></table>
   * @exception IOException if an I/O error occurs
   */
  public int crank () throws IOException
  {
    if (closed)
      return EOI;
    int code = in.readBits (codeSize);
    if (code == EOD)
      return EOD;
    else if (code == endCode || code == EOI)
      {
        closed = true;
        clearTable ();
        table.trimToSize (); // free space
        return EOI;
      }
    else if (code == clearCode)
      {
        clearTable ();
        cleared = true;
      }
    else if (cleared)
      {
        prev = (Entry) table.elementAt (code);
        out.write (code);
        cleared = false;
      }
    else if (code > nextCode)
      throw new StreamCorruptedException ("Illegal code " + code + " in LZW stream (nextCode = " + nextCode + ")");
    else
      {
        // is this the notorious special case where we see a code
        // that hasn't been assigned a string but can infer what the
        // string must have been?
        boolean special = code == nextCode;
        // if so, the string must begin with the last string encountered,
        // otherwise it's just one of the strings already in the table.
        Entry cur = special ? prev : (Entry) table.elementAt (code);
        // get the first letter
        byte first = cur.recurse ();
        // make a new entry for the string (prev + first)
        Entry next = new Entry ();
        next.lett = first;
        next.dad  = prev;
        table.addElement (next);
        // in the special case, we haven't output the last letter
        // yet, since we didn't know it then -- do this now.
        if (special)
          out.write (first);
        // now remember for the next round which entry we used.
        // normally this is cur, but in the special case cur
        // was just a first guess, and the new entry next
        // (= cur + first) is effectively the one we used.
        prev = special ? next : cur;

        // check whether the code size needs to be increased, possibly
        // taking into account the silly practice of wasting bits.
        // Fellow scientists!!
        if (++nextCode == maxCode - earlyChange)
          if (codeSize != maxBits) // not sure why there's no "else" here
            {
              codeSize++;
              maxCode <<= 1;
            }
      }

    return OK;
  }
}
