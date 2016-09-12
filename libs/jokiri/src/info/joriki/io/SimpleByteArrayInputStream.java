/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.IOException;
import java.io.EOFException;

import java.util.Stack;

public class SimpleByteArrayInputStream extends ReadableInputStream implements Writeable
{
  byte [] buf;
  int pos;
  int count;
  
  public SimpleByteArrayInputStream (byte [] buf)
  {
    this (buf,0,buf.length);
  }

  public SimpleByteArrayInputStream (byte [] buf,int off,int len)
  {
    this.buf = buf;
    this.pos = off;
    this.count = Math.min(off + len, buf.length);
  }

  public int read ()
  {
    return pos < count ? buf [pos++] & 0xff : -1;
  }

  public int read (byte [] b,int off,int len)
  {
    if (b == null)
      throw new NullPointerException ();
    else if ((off < 0) || (off > b.length) || (len < 0) ||
             ((off + len) > b.length) || ((off + len) < 0))
      throw new IndexOutOfBoundsException();
    if (pos >= count)
      return -1;
    if (pos + len > count)
      len = count - pos;
    if (len <= 0)
      return 0;
    System.arraycopy(buf, pos, b, off, len);
    pos += len;
    return len;
  }

  public long skip (long n)
  {
    if (pos + n > count)
      n = count - pos;
    if (n < 0)
      return 0;
    pos += n;
    return n;
  }

  public int available ()
  {
    return count - pos;
  }

  public boolean markSupported ()
  {
    return true;
  }

  Stack markStack = new Stack ();

  public void mark (int readAheadLimit)
  {
    mark ();
  }

  public void mark ()
  {
    markStack.push (new Integer (pos));
  }

  public void reset ()
  {
    pos = ((Integer) markStack.pop ()).intValue ();
  }

  public void close ()
  {
  }

  public int peek ()
  {
    return pos < count ? buf [pos] & 0xff : -1;
  }

  /**
   * Writes a single byte to the underlying byte array.
   * @param b the byte to be written
   * @exception EOFException if the write would extend
   * beyond the end of the seekable byte array
   */
  public void write (int b) throws EOFException
  {
    if (pos >= count)
      throw new EOFException ();
    buf [pos++] = (byte) b;
  }

  /**
   * Writes an entire byte array to the underlying byte array.
   * @param b the byte array to be written
   * @exception EOFException if the write would extend
   * beyond the end of the seekable byte array
   */
  public void write (byte [] b) throws EOFException
  {
    write (b,0,b.length);
  }
  
  /**
   * Writes a section of a byte array to the underlying byte array.
   * @param b the byte array containg the data to be written
   * @param off the start index of the data to be written
   * @param len the length of the data to be written
   * @exception EOFException if the write would extend
   * beyond the end of the seekable byte array
   */
  public void write (byte [] b,int off,int len) throws EOFException
  {
    if (pos + len > count)
      throw new EOFException ();
    System.arraycopy (b,off,buf,pos,len);
    pos += len;
  }

  /**
   * Writes the specified number of bytes to the specified writeable.
   *
   * The simple byte array input stream is repositioned to the
   * end of the data written.
   * @param out the writeable to be written to
   * @param n the number of bytes to be written
   */     
  public void writeTo (Writeable out,int n) throws IOException
  {
    if (pos + n > count)
      throw new EOFException ();
    out.write (buf,pos,n);
    pos += n;
  }
}
