/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.IOException;

/**
 * A character source is an object from which characters can be read.
 * It is only conceptually different from a byte source.
 * @see CharacterSink
 */
public interface CharacterSource
{
  /**
   * Reads a single character from the character source.
   * @return     the next character
   * @exception  IOException  if an I/O error occurs
   */
  public int read () throws IOException;
}
