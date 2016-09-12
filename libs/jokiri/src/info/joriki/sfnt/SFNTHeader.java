/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.sfnt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import info.joriki.util.Assertions;

public class SFNTHeader extends SFNTTable
{
  final static int headerMagic = 0x5f0f3cf5;

  int version;
  int fontRevision;
  short flags;
  short unitsPerEm;
  long created;
  long modified;

  short xmin,ymin,xmax,ymax;

  short macStyle;
  short smallestReadableSize;
  short fontDirectionHint;
  short indexToLocFormat;
  short glyphDataFormat;

  public SFNTHeader (DataInput in) throws IOException
  {
    super (HEAD);

    version = in.readInt ();
    Assertions.expect (version,0x00010000);
    fontRevision = in.readInt ();
    in.readInt (); // check sum adjustment
    Assertions.expect (in.readInt (),headerMagic);
    flags      = in.readShort ();
    unitsPerEm = in.readShort ();
    if (unitsPerEm != 2048)
      System.out.println ("unusual units per em : " + unitsPerEm);
    created  = in.readLong ();
    modified = in.readLong ();
    xmin = in.readShort ();
    ymin = in.readShort ();
    xmax = in.readShort ();
    ymax = in.readShort ();
    Assertions.expect (xmin <= xmax);
    Assertions.expect (ymin <= ymax);
    macStyle             = in.readShort ();
    Assertions.expect (macStyle & ~0x23,0); // rec_mediakit2008_9-10.pdf uses flag 0x20 (condensed/narrow)
    smallestReadableSize = in.readShort ();
    fontDirectionHint    = in.readShort ();
    Assertions.limit (fontDirectionHint,-2,2);
    indexToLocFormat     = in.readShort ();
    Assertions.limit (indexToLocFormat,0,1);
    glyphDataFormat      = in.readShort ();
    Assertions.expect (glyphDataFormat,0);
  }

  public void writeTo (DataOutput out) throws IOException
  {
    out.writeInt (version);
    out.writeInt (fontRevision);
    out.writeInt (0);
    out.writeInt (headerMagic);
    out.writeShort (flags);
    out.writeShort (unitsPerEm);
    out.writeLong (created);
    out.writeLong (modified);
    out.writeShort (xmin);
    out.writeShort (ymin);
    out.writeShort (xmax);
    out.writeShort (ymax);
    out.writeShort (macStyle);
    out.writeShort (smallestReadableSize);
    out.writeShort (fontDirectionHint);
    out.writeShort (indexToLocFormat);
    out.writeShort (glyphDataFormat);
  }

  public double [] bbox ()
  {
    return new double [] {xmin,ymin,xmax,ymax};
  }

  public void addBox (SFNTHeader header) {
	  xmin = (short) Math.min (xmin,header.xmin);
	  ymin = (short) Math.max (ymin,header.ymin);
	  xmax = (short) Math.min (xmax,header.xmax);
	  ymax = (short) Math.max (ymax,header.ymax);
  }
  
  public short getIndexToLocFormat ()
  {
    return indexToLocFormat;
  }
}
