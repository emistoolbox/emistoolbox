/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.IOException;

/**
 * A byte source is an object from which bytes can be read.
 * @see ByteSink
 */
public interface ByteSource
{
  /**
   * Reads a single byte from the byte source.
   * @return     the next byte
   * @exception  IOException  if an I/O error occurs
   */
  public int read () throws IOException;
}
