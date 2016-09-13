/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.IOException;

/**
 * This interface extends the <code>Seekable</code> interface
 * to include functionality which depends on the object knowing
 * where its data ends.
 */
public interface TerminallySeekable extends Seekable
{
  /**
   * Returns the position of the "end" of the object.
   * This should be the first position at which a call to
   * <code>read()</code> would return <code>-1</code>.
   * @return the first position beyond the end of data
   * @exception IOException if an I/O error occurs
   */
  public long length () throws IOException;

  /**
   * Sets the position relative to the end,
   * as determined by the <code>length</code> method.
   * The call <code>seekFromEnd (delta)</code> should have
   * exactly the same effect as <code>seek (length () + delta)</code>.
   * @param delta the distance from the end of the file
   * @exception IOException if an I/O error occurs
   * @see #seek(long)
   * @see #length()
   */
  public void seekFromEnd (long delta) throws IOException;
}
