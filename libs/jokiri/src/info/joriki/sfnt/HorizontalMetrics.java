/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.sfnt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import info.joriki.util.Assertions;

public class HorizontalMetrics extends SFNTTable
{
  public int [] advanceWidth;
  public short [] leftSideBearing;

  HorizontalMetrics () {
	  super (HMTX);
  }
  
  public HorizontalMetrics (int [] advanceWidth,short [] leftSideBearing) {
	  this ();
	  this.advanceWidth = advanceWidth;
	  this.leftSideBearing = leftSideBearing;
  }

public HorizontalMetrics (DataInput in,HorizontalHeader hhea,int length) throws IOException
  {
	this ();
	
    int nlong = hhea.numOfLongHorMetrics;
    int nshort = length / 2 - 2 * nlong;
    Assertions.expect (nshort >= 0);
    Assertions.expect (4 * nlong + 2 * nshort,length);
    int n = nlong + nshort;
    advanceWidth = new int [n];
    leftSideBearing = new short [n];

    for (int i = 0;i < n;i++)
      {
        advanceWidth [i] = i < nlong ? in.readUnsignedShort () : advanceWidth [nlong - 1];
        leftSideBearing [i] = in.readShort ();
      }
  }

  public void writeTo (DataOutput out) throws IOException
  {
    for (int i = 0;i < advanceWidth.length;i++)
      {
        out.writeShort (advanceWidth [i]);
        out.writeShort (leftSideBearing [i]);
      }
  }

  public int getWidth (int index)
  {
    return advanceWidth [index] & 0xffff;
  }
  
  public int [] getWidths ()
  {
    return advanceWidth;
  }
}
