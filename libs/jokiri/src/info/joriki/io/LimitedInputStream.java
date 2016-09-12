/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * A limited input stream is a wrapper for an input stream
 * that limits the input from that stream to a certain number of bytes.
 */
public class LimitedInputStream extends CountingInputStream
{
  protected int limit;
  
  /**
   * Creates a limited output stream that reads from the specified
   * underlying input stream until the specified number of bytes
   * has been read.
   */
  public LimitedInputStream (InputStream in,int limit)
  {
    super (in);
    this.limit = limit;
  }

  /**
   * Reads a single byte from the underlying input stream if the
   * limit has not been reached.
   * @return the next byte of data, or <code>-1</code> if the end of the stream
   * or the limit imposed on it has been reached
   * @exception IOException if an I/O error occurs
   */
  public int read () throws IOException
  {
    if (count == limit)
      return -1;
    return super.read ();
  }

  /**
   * Reads the specified section of the specified byte array as far
   * as the limit allows.
   * @return the number of bytes actually read, or <code>-1</code>
   * if there is no more data because the end of the stream or
   * the limit imposed on it has been reached
   * @param b the array into which bytes are to be read
   * @param off the offset of the bytes in the array
   * @param len the number of bytes to be read
   * @exception IOException if an I/O error occurs
   */
  public int read (byte [] b,int off,int len) throws IOException
  {
    if (count == limit)
      return -1;
    int bytesLeft = limit - count;
    return super.read (b,off,bytesLeft < len ? bytesLeft : len);
  }

  /**
   * Returns the number of bytes that can be read from the underlying
   * input stream without blocking and without reaching the limit imposed.
   * @return the number of bytes that can be read
   * @exception IOException if an I/O error occurs
   */
  public int available () throws IOException
  {
    return Math.min (in.available (),limit - count);
  }

  /**
   * Tests whether the limit on this input stream has been reached.
   * @return true if and only if a call to <code>read</code> would
   * return <code>-1</code>
   */
  public boolean limitReached ()
  {
    return count == limit;
  }

  /**
   * Skips forward to the limit imposed on this input stream.
   * @exception IOException if an I/O error occurs
   */
  public void skipToLimit () throws IOException
  {
    info.joriki.io.Util.skipBytesExactly (this,limit - count);
  }
}
