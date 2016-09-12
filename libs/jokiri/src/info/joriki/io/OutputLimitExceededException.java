/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.IOException;

/**
 * This exception is thrown if the limit imposed on a limited output
 * stream is exceeded.
 */
public class OutputLimitExceededException extends IOException
{
  /**
   * The number of bytes written before the output limit was reached
   */
  public int written;
  
  /**
   * Creates a new <code>OutputLimitExceededException</code> which reports
   * that the specified number of bytes was written before the limit
   * was reached.
   @param written the number of bytes written before reaching the limit
   */
  OutputLimitExceededException (int written)
  {
    super ("limit imposed on output stream reached after writing " + written + " bytes");
    this.written = written;
  }
}
