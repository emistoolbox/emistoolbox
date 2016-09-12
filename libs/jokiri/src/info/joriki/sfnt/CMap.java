/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.sfnt;

import java.io.IOException;
import java.io.DataOutput;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import info.joriki.io.Util;
import info.joriki.io.FullySeekableDataInput;
import info.joriki.util.Options;
import info.joriki.util.Assertions;
import info.joriki.util.NotImplementedException;

public class CMap extends EncodingDependentTable
{
  public CMap ()
  {
    super (CMAP);
  }
  
  public CMap (FullySeekableDataInput in,int length) throws IOException
  {
    this ();

    int version = in.readUnsignedShort ();
    Assertions.expect (version,0);
    int ntables = in.readUnsignedShort ();

    for (int i = 0;i < ntables;i++)
      {
        short platform = in.readShort ();
        short encoding = in.readShort ();
        int offset = in.readInt ();

        in.mark ();
        in.seek (offset);

        int format = in.readUnsignedShort ();
        CMapTable table;
        switch (format)
          {
          case  0 : table = new ByteEncodingTable (in); break;
          case  4 : table = new SegmentMappingTable (in); break;
          case  6 : table = new TrimmedTable (in); break;
          default : throw new NotImplementedException ("cmap table format " + format);
          }

        table.platform = platform;
        table.encoding = encoding;

        // only Macintosh tables are allowed to have non-zero languages
        if (table.platform != 1 && table.language != 0)
          Options.warn ("Non-Macintosh cmap subtable with non-zero language field");

        addEntry (table);

        in.reset ();
      }
  }

  /*
    public CMap (char [] unicodes)
    {
    SortedSet rangeSet = new TreeSet ();
    int lastCode = 0;
    int firstCode = 0; // initial value not used
    for (int i = 1;i <= unicodes.length;i++)
    {
    int code = i == unicodes.length ? -1 : unicodes [i];
    if (code != lastCode + 1)
    {
    if (lastCode != 0)
    {
    Range range = new Range ();
    range.startCode = firstCode;
    range.endCode = lastCode;
    range.idDelta = i - 1 - lastCode;
    range.idRangeOffset = 0;
    rangeSet.add (range);
    }
    firstCode = code;
    }
    lastCode = code;
    }

    Range lastRange = new Range ();
    lastRange.startCode = 0xffff;
    lastRange.endCode = 0xffff;
    lastRange.idDelta = 1;
    lastRange.idRangeOffset = 0;
    rangeSet.add (lastRange);
    ranges = new Range [rangeSet.size ()];
    rangeSet.toArray (ranges);
    glyphIdArray = new int [0];
    }
  */

  public void writeTo (DataOutput out) throws IOException
  {
    int ntable = entries.size ();

    out.writeShort (0); // version
    out.writeShort (ntable);

    DataBlockList dataBlocks = new DataBlockList (); 
    dataBlocks.offset = 4 + 8 * ntable;

    Iterator iterator = entries.values ().iterator ();

    for (int i = 0;i < ntable;i++)
      {
        CMapTable table = (CMapTable) iterator.next ();
        DataBlock dataBlock = new DataBlock (Util.toByteArray (table));
        dataBlocks.add (dataBlock);
        out.writeShort (table.platform);
        out.writeShort (table.encoding);
        out.writeInt (dataBlock.offset);
      }

    for (DataBlock dataBlock : dataBlocks)
      out.write (dataBlock.data);
  }

  public CMapTable getTable (int platform,int encoding,int language)
  {
    return (CMapTable) get (new EncodingScheme (platform,encoding,language));
  }

  public int [] getUnicodes (int platform,int encoding,int language)
  {
    return getTable (platform,encoding,language).getUnicodes ();
  }

  public CMapTable getTable (int platform,int encoding)
  {
    CMapTable unspecific = getTable (platform,encoding,0);
    if (unspecific != null)
      return unspecific;
    Iterator iterator = entries.keySet ().iterator ();
    while (iterator.hasNext ())
      {
        EncodingScheme scheme = (EncodingScheme) iterator.next ();
        if (scheme.platform == platform && scheme.encoding == encoding)
          return (CMapTable) entries.get (scheme);
      }
    return null;
  }
}
