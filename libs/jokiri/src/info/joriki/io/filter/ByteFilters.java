/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io.filter;

import java.io.IOException;

import info.joriki.io.Util;
import info.joriki.io.Readable;
import info.joriki.io.Writeable;

/**
 * This package provides methods which allow byte-oriented input/output
 * filters to be used with a more convenient syntax closer to the one
 * used by filtered streams in <code>java.io</code>.
 */
public class ByteFilters
{
  private ByteFilters () {}

  /**
   * Sets the specified input filter up to read from the specified readable
   * and returns a readable for reading from the filter.
   * @param filter the filter to be set up
   * @param in the readable to be read from
   * @return a readable for reading from the filter
   */
  public static Readable getReadable (InputFilter filter,Readable in)
  {
    filter.setSource (in);
    return (Readable) filter.getSource ();
  }

  /**
   * Sets the specified output filter up to write to the specified writeable
   * and returns a writeable for writing to the filter.
   * @param filter the filter to be set up
   * @param in the writeable to be written to
   * @return a writeable for writing to the filter
   */
  public static Writeable getWriteable (OutputFilter filter,Writeable out)
  {
    filter.setSink (out);
    return (Writeable) filter.getSink ();
  }

  /**
   * Combines the specified converter with a byte buffer, sets up the
   * resulting input filter to read from the specified readable
   * and returns a readable for reading from the filter.
   * @param converter the converter to be used
   * @param in the readable to be read from
   * @return a readable for reading from the filter
   */
  public static Readable getReadable (Converter converter,Readable in)
  {
    return getReadable
      (Concatenator.concatenate
       (converter,new ByteBuffer ()),
       in);
  }

  public static void copy (Readable in,
                           Writeable out,
                           Filter [] filters) throws IOException
  {
    if (filters [0] instanceof InputFilter)
      {
        InputFilter filter = (InputFilter) filters [0];
        for (int i = 1;i < filters.length;i++)
          filter = Concatenator.concatenate (filter,(InputFilter) filters [i]);
        Util.copy (getReadable (filter,in),out);
      }
    else
      {
        OutputFilter filter = (OutputFilter) filters [0];
        for (int i = 1;i < filters.length;i++)
          filter = Concatenator.concatenate (filter,(OutputFilter) filters [i]);
        Util.copy (in,getWriteable (filter,out));
      }
  }
}
