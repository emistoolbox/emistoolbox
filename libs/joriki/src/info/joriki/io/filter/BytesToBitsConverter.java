/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io.filter;

import java.io.IOException;

import info.joriki.io.BitSink;

/**
   This class reads bytes and writes them as bits.
*/
public class BytesToBitsConverter extends ChameleonSourceRecipient implements Crank
{
  BitSink out;

  /**
     Sets the sink that this converter writes to.
     The sink must implement the <code>BitSink</code> interface.
     @param sink the sink to be written to
  */
  public void setSink (Object sink)
  {
    out = (BitSink) sink;
  }

  /**
     Reads a single byte from the source and writes it
     to the sink as eight bits.
     @return
     <table><tr valign=top><td>
     {@link Filter#OK OK}
     </td><td>
     if a byte was successfully read and written as eight bits
     </td></tr><tr valign=top><td>
     {@link Filter#EOI EOI}
     </td><td>
     if the source returned <code>EOI</code>
     </td></tr><tr valign=top><td>
     {@link Filter#EOD EOD}
     </td><td>
     if the source returned <code>EOD</code>
     </td></tr></table>
     @exception IOException if either the read or the
     write operation caused an <code>IOException</code>
  */
  public int crank () throws IOException
  {
    int b = fromStream ? inputStream.read () : byteSource.read ();
    if (b < 0)
      {
        if (b == EOI)
          out.close ();
        return b;
      }
    out.writeBits (b,8);
    return OK;
  }
}
