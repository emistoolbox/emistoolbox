/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.sfnt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import info.joriki.io.Util;

public class GenericTable extends SFNTTable
{
  byte [] data;

  public GenericTable (DataInput in,String id,int length) throws IOException
  {
    this (id,Util.readBytes (in,length));
  }

  public GenericTable (String id,byte [] data)
  {
    super (id);

    this.data = data;
  }

  public void writeTo (DataOutput out) throws IOException
  {
    out.write (data);
  }

  public byte [] toByteArray ()
  {
    return data;
  }
}
