/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.IOException;

/**
 * This interface, lamentably absent in java.io, abstracts the common
 * functionality of input streams and random access files.
 * @see Writeable
 */
public interface Readable extends ByteSource
{
  /**
   * Reads data from this readable into an array of bytes. 
   * @param      b   the array to be read into
   * @return     the total number of bytes actually read into the array, or
   *             <code>-1</code> if the end of data has been reached
   * @exception  IOException  if an I/O error occurs
   */
  public int read (byte [] b) throws IOException;
  /**
   * Reads data from this readable into an array of bytes. 
   * @param      b   the array to be read into
   * @param      off the start offset in the array
   * @param      len the number of bytes to be read
   * @return     the total number of bytes actually read into the array, or
   *             <code>-1</code> if the end of data has been reached
   * @exception  IOException  if an I/O error occurs
   */
  public int read (byte [] b,int off,int len) throws IOException;
  /**
   * Returns the number of bytes that can be read
   * from this readable without blocking.
   * @return     the number of bytes that can be read
   * from this readable without blocking.
   * @exception  IOException  if an I/O error occurs
   */
  public int available () throws IOException;
  /**
   * Closes the readable.
   * @exception  IOException  if an I/O error occurs
   */
  public void close () throws IOException;
}
