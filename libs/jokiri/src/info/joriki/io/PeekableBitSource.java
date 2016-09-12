/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.IOException;

public interface PeekableBitSource extends BitSource
{
  /**
   * Peeks at up to 31 bits without consuming them,
   * cranking the input crank if necessary.
   * @param n the number of bits to be peeked at
   * @return the bits peeked at, or <code>-1</code> if the
   * end of file is reached
   */
  int peekBits (int n) throws IOException;
  /**
   * Drops the specified number of bits from this bit source.
   * @param n the number of bits to be dropped
   */
  void dropBits (int n);
  /**
   * Aligns the bit source on a byte boundary.
   */
  void byteAlign ();
  /**
   * Resets this bit source.
   */
  void reset ();
}
