/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.IOException;
import java.io.OutputStream;

import info.joriki.io.filter.OutputFilter;

/**
   A bit sink output stream is an output stream that passes any data
   written to it through to a bit sink.
*/
public class BitSinkOutputStream extends OutputStream implements OutputFilter, Writeable
{
  BitSink sink;

  /**
     Writes a byte of data to the underlying bit sink as eight bits.
     @param b the byte to be written
  */
  final public void write (int b) throws IOException
  {
    sink.writeBits (b,8);
  }

  /**
     Sets the bit sink that this bit sink output stream writes to.
     @param sink the sink to be written to
  */
  public void setSink (Object sink)
  {
    this.sink = (BitSink) sink;
  }

  /**
     Returns the sink provided by this bit sink output stream,
     namely the bit sink output stream itself.
     @return the sink provided by this bit sink output stream
  */
  public Object getSink ()
  {
    return this;
  }
}
