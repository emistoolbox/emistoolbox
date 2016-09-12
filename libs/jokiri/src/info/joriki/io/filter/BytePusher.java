/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io.filter;

import java.io.IOException;

import info.joriki.io.Readable;
import info.joriki.io.Writeable;

/**
   A byte pusher is an identity converter for bytes which
   can be used to connect a passive byte output to a passive
   byte input.
*/
public class BytePusher implements Crank
{
  Readable in;
  Writeable out;

  /**
     Sets the source that this byte pusher reads from.
     The source must implement the <code>Readable</code> interface.
     @param source the source to be read from
  */
  public void setSource (Object source)
  {
    this.in = (Readable) source;
  }

  /**
     Sets the sink that this byte pusher writes to.
     The sink must implement the <code>Writeable</code> interface.
     @param sink the sink to be written to
  */
  public void setSink (Object sink)
  {
    this.out = (Writeable) sink;
  }

  /**
     Reads a single byte from the source and writes it
     to the sink.
     @return
     <table><tr valign=top><td>
     {@link Filter#OK OK}
     </td><td>
     if a byte could be pushed
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
    int b = in.read ();
    if (b != OK)
      {
        if (b == EOI)
          out.close ();
        return b;
      }
    out.write (b);
    return OK;
  }
}
