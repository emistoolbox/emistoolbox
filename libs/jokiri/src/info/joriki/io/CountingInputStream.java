/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.EOFException;
import java.io.InputStream;
import java.io.IOException;

/**
 * A counting input stream is a wrapper for an input stream
 * that keeps count of the number of bytes read.
 */
public class CountingInputStream extends ReadableFilterInputStream
{
  /**
   * The number of bytes read so far
   */
  protected int count = 0;

  /**
   * Creates a counting input stream from the specified input stream.
   * @param in the input stream to be wrapped
   */
  public CountingInputStream (InputStream in)
  {
    super (in);
  }

  /**
   * Reads a single byte from the underlying input stream
   * and increments the byte count.
   * @return the next byte of data, or <code>-1</code> if the
   * end of the stream has been reached
   * @exception IOException if an I/O error occurs
   */
  public int read () throws IOException
  {
    int b = in.read ();
    if (b >= 0)
      count++;
    return b;
  }

  /**
   * Reads the specified section of the specified byte array and increases
   * the byte count accordingly.
   * @return the number of bytes actually read, or <code>-1</code> if
   * there is no more data because the end of the stream has been reached
   * @param b the array into which bytes are to be read
   * @param off the offset of the bytes in the array
   * @param len the number of bytes to be read
   * @exception IOException if an I/O error occurs
   */
  public int read (byte [] b,int off,int len) throws IOException
  {
    int read = super.read (b,off,len);
    if (read != -1)
      count += read;
    return read;
  }

  /**
   * Attempts to skip over and discard <code>n</code> bytes and increases
   * the byte count accordingly.
   * @return the actual number of bytes skipped.
   * @param n the number of bytes to be skipped.
   * @exception IOException if an I/O error occurs
   */
  public long skip (long n) throws IOException
  {
    long skipped = super.skip (n);
    count += skipped;
    return skipped;
  }

  /**
   * Returns the number of bytes read so far.
   * @return the number of bytes read so far
   */
  public int getCount ()
  {
    return count;
  }
  
  int markCount;
  
  public void mark (int readLimit) {
    super.mark (readLimit);
    markCount = count;
  }
  
  public void reset () throws IOException {
    super.reset ();
    count = markCount;
  }
  
  /**
   * Skips bytes up to byte count <code>pos</code>.
   * @param pos the byte count to be sought
   * @exception EOFException if the end of the stream is reached before byte count <code>pos</code>
   * @exception IOException if byte count <code>pos</code> has been exceeded
   * @exception IOException if an I/O error occurs
   */
  public void seek (long pos) throws IOException
  {
    while (count < pos)
      if (skip (pos - count) == 0)
        throw new EOFException ();
    if (count > pos)
      throw new IOException ();
  }
  
  /**
   * Resets the count for this counting input stream to zero.
   */
  public void resetCount () {
    count = 0;
  }
}
