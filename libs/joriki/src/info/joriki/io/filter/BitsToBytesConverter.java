/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io.filter;

import java.io.IOException;

import info.joriki.io.BitSource;
import info.joriki.io.Writeable;

/**
   This class reads bits and writes them as bytes.
*/
public class BitsToBytesConverter implements Crank
{
  BitSource in;
  Writeable out;

  /**
     Sets the source that this converter reads from.
     The source must implement the <code>BitSource</code> interface.
     @param source the source to be read from
  */
  public void setSource (Object source)
  {
    in = (BitSource) source;
  }

  /**
     Sets the sink that this converter writes to.
     The sink must implement the <code>Writeable</code> interface.
     @param sink the sink to be written to
  */
  public void setSink (Object sink)
  {
    out = (Writeable) sink;
  }

  /**
     Reads eight bits from the source and
     writes them to the sink as a byte.
     @return
     <table><tr valign=top><td>
     {@link Filter#OK OK}
     </td><td>
     if eight bits were successfully read and written as a byte
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
    int b = in.readBits (8);
    if (b < 0)
      {
        if (b == EOI)
          out.close ();
        return b;
      }
    out.write (b);
    return OK;
  }
}
