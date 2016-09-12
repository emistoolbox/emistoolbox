/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.IOException;

public interface Markable extends Readable
{
  /**
   * Marks the current position.
   * @param readLimit the maximum number of bytes that can be read
   *                  before the mark position becomes invalid 
   * @exception  IOException if an I/O error occurs
   */
  public void mark (int readLimit) throws IOException;

  /**
   * Returns to the marked position.
   * @exception  IOException if an I/O error occurs
   */
  public void reset () throws IOException;
}
