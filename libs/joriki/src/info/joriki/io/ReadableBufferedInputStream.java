/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.InputStream;
import java.io.BufferedInputStream;

/**
 * A readable buffered input stream is a buffered input stream 
 * made to implement the interface <code>Readable</code>.
 */
public class ReadableBufferedInputStream
  extends BufferedInputStream
  implements Readable
{
  /**
   * Creates a readable buffered input stream to read from the specified
   * input stream.
   * @param in the input stream to be read from
   */
  public ReadableBufferedInputStream (InputStream in)
  {
    super (in);
  }

  /**
   * Creates a readable buffered input stream with the specified
   * buffer size to read from the specified input stream.
   * @param in the input stream to be read from
   * @param size the buffer size
   */
  public ReadableBufferedInputStream (InputStream in,int size)
  {
    super (in,size);
  }
}
