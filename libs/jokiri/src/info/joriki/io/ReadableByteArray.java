/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.ByteArrayInputStream;

/**
 * A readable byte array is a byte array input stream
 * made to implement the interface <code>Readable</code>.
 */
public class ReadableByteArray extends ByteArrayInputStream implements Readable
{
  /**
   * Creates a readable byte array to read from the specified byte array.
   * @param b the byte array to be read from
   */
  public ReadableByteArray (byte [] b)
  {
    super (b);
  }

  /**
   * Creates a readable byte array to read from the specified section
   * of the specified byte array.
   * @param b the byte array to be read from
   * @param off the offset of the section in the array
   * @param len the number of bytes in the secion
   */
  public ReadableByteArray (byte [] b,int off,int len)
  {
    super (b,off,len);
  }
}  

