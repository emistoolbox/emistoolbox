/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.InputStream;
import java.io.FilterInputStream;

/**
 * A readable filter input stream is a filter input stream made
 * to implement the interface <code>Readable</code>.
 */
public class ReadableFilterInputStream extends FilterInputStream implements Readable
{
  /**
   * Creates a readable filter input stream from the specified underlying
   * input stream.
   * @param in the underlying input stream
   */
  public ReadableFilterInputStream (InputStream in)
  {
    super (in);
  }
}
