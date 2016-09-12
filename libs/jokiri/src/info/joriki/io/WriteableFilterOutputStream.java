/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.OutputStream;
import java.io.FilterOutputStream;

/**
 * A writeable filter output stream is a filter output stream made
 * to implement the interface <code>Writeable</code>.
 */
public class WriteableFilterOutputStream extends FilterOutputStream implements Writeable
{
  /**
   * Creates a writeable filter output stream from the specified underlying
   * output stream.
   * @param out the underlying output stream
   */
  public WriteableFilterOutputStream (OutputStream out)
  {
    super (out);
  }
}
