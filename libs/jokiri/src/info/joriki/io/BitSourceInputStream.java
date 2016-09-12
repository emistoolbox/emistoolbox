/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.IOException;
import info.joriki.io.filter.InputFilter;

/**
   A bit source input stream is an input stream that passes
   data read from a bit source on as bytes.
*/
public class BitSourceInputStream extends ReadableInputStream implements InputFilter
{
  BitSource source;

  /**
     Reads eight bits from the underlying bit source and returns them as a byte.
     @return the next byte of data
  */
  final public int read () throws IOException
  {
    return source.readBits (8);
  }

  /**
     Sets the bit source that this bit source input stream reads from.
     @param source the bit source to be read from
  */
  public void setSource (Object source)
  {
    this.source = (BitSource) source;
  }

  /**
     Returns the source provided by this bit source input stream,
     namely the bit source input stream itself.
     @return the source provided by this bit source input stream
  */
  public Object getSource ()
  {
    return this;
  }
}
