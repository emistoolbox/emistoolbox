/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.sfnt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import info.joriki.io.Outputable;

import info.joriki.util.Options;

public class BinarySearchData implements Outputable
{
  int searchRange;
  int entrySelector;
  int rangeShift;
  
  BinarySearchData (int nUnits,int unitSize)
  {
    int pow = 1;
    entrySelector = 0;
    while ((pow <<= 1) <= nUnits)
      entrySelector++;
    pow >>= 1;
    searchRange = unitSize * pow;
    rangeShift = unitSize * (nUnits - pow);
  }

  void assertFrom (DataInput in) throws IOException
  {
    if (in.readUnsignedShort () != searchRange | // non-abortive or
        in.readUnsignedShort () != entrySelector |
        in.readUnsignedShort () != rangeShift)
      Options.warn ("Non-standard binary search data");
  }

  public void writeTo (DataOutput out) throws IOException
  {
    out.writeShort (searchRange);
    out.writeShort (entrySelector);
    out.writeShort (rangeShift);
  }
}
