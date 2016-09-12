/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.IOException;

/**
 * A byte sink is an object to which bytes can be written.
 * @see ByteSource
 */
public interface ByteSink
{
  /**
   * Writes a single byte to the byte sink.
   * @param b    the byte to be written
   * @exception  IOException  if an I/O error occurs
   */
  public void write (int b) throws IOException;
}
