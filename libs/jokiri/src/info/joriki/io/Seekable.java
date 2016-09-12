/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.IOException;

/**
 * This interface combines the functionality of readables, writeables
 * and markables and adds the seeking capability of random access files.
 */
public interface Seekable extends Writeable, Markable
{
  /**
   * Seeks to a specified position.
   * @param   pos   the position to be sought
   * @exception  IOException  if an I/O error occurs
   */
  void seek (long pos) throws IOException;

  /**
   * Marks the current position, seeks to the specified position and
   * causes positions to be measured relative to this new position.
   * @param   pos   the position to be used
   * @see #popOffset
   */
  void pushOffset (long pos) throws IOException;
  
  /**
   * Undoes the previous call to {@link pushOffset}. 
   */
  void popOffset () throws IOException;
  
  /**
   * Returns the offset relative to which positions are currently measured.
   * @return the offset relative to which positions are currently measured
   */
  long getOffset ();
}
