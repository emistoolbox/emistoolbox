/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.IOException;

/**
 * A bit source is an object from which bits can be read.
 * @see BitSink
 */
public interface BitSource
{
  /**
   * Reads the specified number of bits from the bit source
   * @param n the number of bits to be read
   * @return the next <code>n</code>bits from the bit source
   */
  int readBits (int n) throws IOException;
}
