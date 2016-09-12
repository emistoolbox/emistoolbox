/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.sfnt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import info.joriki.util.Options;
import info.joriki.util.Version;
import info.joriki.util.Assertions;

public class HorizontalHeader extends SFNTTable
{
  Version version;
  short ascent;
  short descent;
  short lineGap;
  int advanceWidthMax;
  short minLeftSideBearing;
  short minRightSideBearing;
  short xMaxExtent;
  short caretSlopeRise;
  short caretSlopeRun;
  short caretOffset;
  short format;
  public int numOfLongHorMetrics;

  public HorizontalHeader (DataInput in) throws IOException
  {
    super (HHEA);

    version = new Version (in);
    Assertions.expect (version.major,1);
    Assertions.expect (version.minor,0);
    ascent              = in.readShort ();
    descent             = in.readShort ();
    lineGap             = in.readShort ();
    advanceWidthMax     = in.readUnsignedShort ();
    minLeftSideBearing  = in.readShort ();
    minRightSideBearing = in.readShort ();
    xMaxExtent          = in.readShort ();
    caretSlopeRise      = in.readShort ();
    caretSlopeRun       = in.readShort ();
    caretOffset         = in.readShort ();
    for (int i = 0;i < 8;i++)
      if (in.readByte () != 0)
        Options.warn ("non-zero reserved field in horizontal header");
    format              = in.readShort ();
    Assertions.expect (format,0);
    numOfLongHorMetrics = in.readUnsignedShort ();
  }

  public void writeTo (DataOutput out) throws IOException
  {
    version.writeTo (out);
    out.writeShort (ascent);
    out.writeShort (descent);
    out.writeShort (lineGap);
    out.writeShort (advanceWidthMax);
    out.writeShort (minLeftSideBearing);
    out.writeShort (minRightSideBearing);
    out.writeShort (xMaxExtent);
    out.writeShort (caretSlopeRise);
    out.writeShort (caretSlopeRun);
    out.writeShort (caretOffset);
    for (int i = 0;i < 8;i++)
      out.write (0);
    out.writeShort (format);
    out.writeShort (numOfLongHorMetrics);
  }
}
