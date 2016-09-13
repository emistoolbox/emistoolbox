/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.IOException;

/*
 * This interface which combines the functionality of {@link FullySeekable}
 * and {@link MarkableDataInput}.
 */
public interface FullySeekableDataInput extends FullySeekable, MarkableDataInput
{
  /**
   * Marks the current position such that a call to {@link reset} returns
   * to it independent of any intervening calls, including nested calls
   * to mark and reset.
   * @exception  IOException if an I/O error occurs
   */
  public void mark () throws IOException;
}
