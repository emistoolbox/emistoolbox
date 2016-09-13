/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.compression.runlength;

import java.io.IOException;

import info.joriki.io.ByteSink;
import info.joriki.io.ByteSource;

import info.joriki.io.filter.Crank;

public class RunLengthDecoder implements RunLengthCoder, Crank
{
  boolean closed = false;
  int remaining = 0;
  boolean copy;

  ByteSource in;
  ByteSink out;

  public void setSource (Object source)
  {
    in = (ByteSource) source;
  }

  public void setSink (Object sink)
  {
    out = (ByteSink) sink;
  }

  public int crank () throws IOException
  {
    if (closed)
      return EOI;

    if (remaining == 0)
      {
        int n = in.read ();
        if (n < 0)
          return n;
        else if (n < RunLengthCoder.EOD)
          {
            remaining = n + 1;
            copy = true;
          }
        else if (n == RunLengthCoder.EOD)
          {
            closed = true;
            return EOI;
          }
        else
          {
            remaining = 257 - n;
            copy = false;
          }
      }

    int b = in.read ();
    if (b < 0)
      return b;

    while (remaining > 0)
      {
        out.write (b);
        remaining--;
        if (copy)
          return OK;
      }

    return OK;
  }
}
