/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.IOException;

/**
 * A persistent input stream reads persistently from an underlying
 * relatively seekable object. When reading from the underlying
 * object, it seeks to the position to be read from, reads and
 * then repositions the file pointer. A persistent input stream
 * therefore neither interferes with nor is interfered with by
 * other activities on the underlying relatively seekable object.
 * Typically, a persistent input stream should be used in
 * conjunction with a buffered input stream in order to reduce
 * the amount of seeking necessary.
 */

public class PersistentInputStream extends ReadableInputStream
{
  RelativelySeekable seekable;
  long position;

  /**
   * Constructs a persistent input stream that will read from the
   * specified relatively seekable object starting at position 0.
   * @param seekable the relatively seekable object to be read from
   */
  public PersistentInputStream (RelativelySeekable seekable)
  {
    this (seekable,0);
  }

  /**
   * Constructs a persistent input stream that will read from the
   * specified relatively seekable object starting at the specified
   * position.
   * @param seekable the relatively seekable object to be read from
   * @param position the position to start at
   */
  public PersistentInputStream (RelativelySeekable seekable,long position)
  {
    this.seekable = seekable;
    this.position = position;
  }

  /**
   * Reads a single byte.
   * @return the next byte, or <code>-1</code> if the end of the
   * stream has been reached
   * @exception IOException if an I/O error occurs
   */
  public int read () throws IOException
  {
    long where = there ();
    int b = seekable.read ();
    if (b != -1)
      position++;
    seekable.seek (where);
    return b;
  }
      
  /**
   * Reads the specified section of the specified byte array.
   * @return the number of bytes actually read, or <code>-1</code>
   * if the end of the stream has been reached
   * @param b the array into which bytes are to be read
   * @param off the offset of the bytes in the array
   * @param len the number of bytes to be read
   * @exception IOException if an I/O error occurs
   */
  public int read (byte [] b,int off,int len) throws IOException
  {
    long where = there ();
    int n = seekable.read (b,off,len);
    if (n != -1)
      position += n;
    seekable.seek (where);
    return n;
  }

  final private long there () throws IOException
  {
    long where = seekable.getFilePointer ();
    if (where != position)
      seekable.seek (position);
    return where;
  }
}
