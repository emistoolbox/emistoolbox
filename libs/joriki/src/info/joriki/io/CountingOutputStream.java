/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.OutputStream;
import java.io.IOException;

/**
 * A counting output stream is a wrapper for an output stream
 * that keeps count of the number of bytes written.
 * <BR>
 * This class knows about output limits and keeps a correct
 * count if it intercepts an {@link OutputLimitExceededException}.
 */
public class CountingOutputStream extends WriteableFilterOutputStream
{
  /**
   * The number of bytes written so far
   */
  protected int count = 0;

  /**
   * Constructs a counting output stream from the specified output stream.
   * @param out the output stream to be wrapped
   */
  public CountingOutputStream (OutputStream out)
  {
    super (out);
  }

  /**
   * Writes a single byte to the underlying output stream and increments
   * the byte count
   * @param b the byte to be written
   * @exception IOException if an I/O error occurs
   */
  public void write (int b) throws IOException
  { 
    out.write (b);
    count++;
  }
  
  /**
   * Writes the specified section of the specified byte array to the
   * underlying output stream and increases the byte count accordingly.
   * @param b the array containing the bytes to be written
   * @param off the offset of the bytes in the array
   * @param len the number of bytes to be written
   * @exception IOException if an I/O error occurs
   */
  public void write (byte [] b,int off,int len) throws IOException
  {
    try {
      out.write (b,off,len);
      count += len;
    } catch (OutputLimitExceededException olxe) {
      count += olxe.written;
      throw olxe;
    }
  }

  /**
   * Returns the number of bytes written so far.
   * @return the number of bytes written so far
   */
  public int getCount ()
  {
    return count;
  }
  
  int delta;

  public void setOffsetCount (int offsetCount) {
    delta = offsetCount - count;
  }
  
  public int getOffsetCount () {
    return count + delta;
  }
  
  public void resetCount () {
    count = 0;
  }
}
