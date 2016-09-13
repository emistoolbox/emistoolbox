/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.IOException;

/**
 * A bit sink is an object to which bits can be written.
 * @see BitSource
 */
public interface BitSink
{
  /**
     Writes bits to the bit sink. The <code>n</code> least
     significant bits of <code>b</code> are written.
     @param b the bits to be written
     @param n the number of bits to be written
     @exception IOException if an I/O error occurs
  */
  void writeBits (int b,int n) throws IOException;
  /**
     Closes the bit sink.
     @exception IOException if an I/O error occurs
  */
  void close () throws IOException;
}
