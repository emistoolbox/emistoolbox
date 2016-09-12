/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.truetype;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import info.joriki.sfnt.SFNTTable;

public class ControlValueTable extends SFNTTable
{
  short [] values;

  public ControlValueTable (DataInput in,int length) throws IOException
  {
    super (CVT);

    values = new short [length >> 1];
    for (int i = 0;i < values.length;i++)
      values [i] = in.readShort ();
  }

  public void writeTo (DataOutput out) throws IOException
  {
    for (int i = 0;i < values.length;i++)
      out.writeShort (values [i]);
  }

  public double [] toDoubleArray ()
  {
    double [] arr = new double [values.length];
    for (int i = 0;i < arr.length;i++)
      arr [i] = values [i];
    return arr;
  }
}
