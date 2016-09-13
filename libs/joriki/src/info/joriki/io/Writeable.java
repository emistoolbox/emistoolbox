/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.IOException;

/**
 * This interface, lamentably absent in java.io, abstracts the common
 * functionality of output streams and random access files.
 * @see Readable
 */
public interface Writeable extends ByteSink
{
  /**
   * Writes an array of bytes to this writeable. 
   * @param      b   the array of bytes to be written
   * @exception  IOException  if an I/O error occurs
   */
  public void write (byte [] b) throws IOException;
  /**
   * Writes data from an array of bytes to this writeable. 
   * @param      b   the array of bytes to be written
   * @param      off the start offset in the array
   * @param      len the number of bytes to be written
   * @exception  IOException  if an I/O error occurs
   */
  public void write (byte [] b,int off,int len) throws IOException;
  /**
   * Closes the writeable.
   * @exception  IOException  if an I/O error occurs
   */
  public void close () throws IOException;
}
