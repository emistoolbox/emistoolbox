/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.compression.ascii;

import java.io.IOException;
import java.io.StreamCorruptedException;

import info.joriki.io.ByteSource;
import info.joriki.io.ByteSink;

import info.joriki.io.filter.Crank;

import info.joriki.util.General;

abstract public class BaseDecoder implements Crank
{
  boolean closed;
  long sofar;
  int nascii;

  ByteSource in;
  ByteSink out;

  int base;
  int ndigit;
  int nout;

  public void setSource (Object source)
  {
    in = (ByteSource) source;
  }

  public void setSink (Object sink)
  {
    out = (ByteSink) sink;
  }

  BaseDecoder (int base,int ndigit,int nout)
  {
    this.base = base;
    this.ndigit = ndigit;
    this.nout = nout;
    reset ();
  }

  public void reset ()
  {
    sofar = 0;
    nascii = 0;
    closed = false;
  }

  // write nascii - 1 bytes of data
  protected void write () throws IOException
  {
    if (sofar >= 0x100000000L)
      throw new StreamCorruptedException ();
    int word = (int) sofar;
    int lim = (ndigit - nascii) << 3;
    int shift = nout << 3;
    while ((shift -= 8) >= lim)
      out.write (word >> shift);
  }

  protected void addDigit (int digit) throws IOException
  {
    sofar *= base;
    sofar += digit;
    if (++nascii == ndigit)
      {
        write ();
        nascii = 0;
        sofar = 0;
      }
  }
  
  public int crank () throws IOException
  {
    if (closed)
      return EOI;

    int ascii;

    do
      ascii = in.read ();
    while (ascii == 0 || General.isWhiteSpace (ascii));

    if (ascii < 0)
      return ascii;

    return addAscii (ascii);
  }

  public int close () throws IOException
  {
    if (nascii == 0)
      return EOI;
    if (nascii == 1)
      throw new StreamCorruptedException ();
    while (nascii != 0)
      addDigit (base - 1); // important for Base85; could add zeroes for Base64
    closed = true;
    return OK;
  }

  protected abstract int addAscii (int ascii) throws IOException;
}
