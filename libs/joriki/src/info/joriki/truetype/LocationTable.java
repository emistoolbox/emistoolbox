/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.truetype;

import java.io.DataOutput;
import java.io.IOException;

import info.joriki.io.FullySeekableDataInput;

import info.joriki.sfnt.SFNTTable;
import info.joriki.sfnt.SFNTHeader;

public class LocationTable extends SFNTTable
{
  public short format;
  int [] offsets;

  private LocationTable ()
  {
    super (LOCA);
  }

  public LocationTable (int [] offsets)
  {
    this ();
    this.offsets = offsets;
    chooseFormat ();
  }

  public short chooseFormat ()
  {
    return format = (short) (offsets [offsets.length - 1] > 0x1ffff ? 1 : 0);
  }

  public LocationTable (FullySeekableDataInput in,SFNTHeader head,int length) throws IOException
  {
    this ();

    format = head.getIndexToLocFormat ();

    offsets = new int [length / (format == 0 ? 2 : 4)];

    for (int i = 0;i < offsets.length;i++)
      offsets [i] = format == 0 ? (in.readUnsignedShort () << 1) : in.readInt ();
  }

  public void writeTo (DataOutput out) throws IOException
  {
    chooseFormat ();
    for (int i = 0;i < offsets.length;i++)
      if (format == 0)
        out.writeShort (offsets [i] >> 1);
      else
        out.writeInt (offsets [i]);
  }
}
