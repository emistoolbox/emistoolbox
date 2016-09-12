/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import info.joriki.util.CloneableObject;

public class ByteStream extends CloneableObject implements ByteSource
{
  public byte [] buf;
  public int pos;

  public ByteStream (byte [] buf)
  {
    this (buf,0);
  }

  public ByteStream (byte [] buf,int pos)
  {
    this.buf = buf;
    this.pos = pos;
  }

  public int read ()
  {
    return pos < buf.length ? buf [pos++] & 0xff : -1;
  }

  public byte readByte ()
  {
    return buf [pos++];
  }

  public void skip (int n)
  {
    pos += n;
  }
}
