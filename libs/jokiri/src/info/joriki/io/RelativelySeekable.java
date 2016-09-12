/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.IOException;

/**
 * This interface extends the <code>Seekable</code> interface
 * to include functionality which depends or may depend on the
 * object knowing its current position.
 */
public interface RelativelySeekable extends Seekable
{
  /**
   * Returns the current position.
   * @return the current position
   * @exception  IOException  if an I/O error occurs
   */
  public long getFilePointer () throws IOException;
  /**
   * Sets the position relative to the current position,
   * as determined by the <code>getFilePointer</code> method.
   * The call <code>seekRelative (delta)</code> should have
   * exactly the same effect as <code>seek (getFilePointer () + delta)</code>.
   * @param delta the distance from the current position
   * @see #seek(long)
   * @see #getFilePointer()
   */
  public void seekRelative (long delta) throws IOException;
}
