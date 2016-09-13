/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.sfnt;

import java.io.DataOutput;
import java.io.IOException;
import java.io.DataOutputStream;
import java.util.Set;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Iterator;

import info.joriki.io.Util;
import info.joriki.io.Outputable;
import info.joriki.io.DataOutputMultiplexer;
import info.joriki.util.Assertions;
import info.joriki.truetype.AbstractGlyphTable;
import info.joriki.truetype.GlyphTable;
import info.joriki.truetype.LocationTable;

public class SFNTDocument implements SFNTSpeaker, Outputable
{
  final static int unitSize = 0x10; // table list entry size

  int magic;

  HashMap tables = new HashMap ();

  public SFNTDocument (int magic)
  {
    this.magic = magic;
  }

  // shallow clone of the document: use all its tables
  // There used to be a separate constructor from an SFNTFile
  // that kept the tables from being read in and written out,
  // but there are advantages to doing that: some fonts waste
  // space, e.g. by using 4-byte instead of 2-byte glyph location
  // offsets, and we check the integrity of the fonts as we read them.
  public SFNTDocument (SFNTDocument document)
  {
    magic = document.magic;
    Iterator idIterator = document.getIDs ().iterator ();
    while (idIterator.hasNext ())
      addTable (document.getTable ((String) idIterator.next ()));
  }

  int checksum (byte [] data)
  {
    SFNTChecksumStream checksumStream = new SFNTChecksumStream ();
    checksumStream.write (data);
    return checksumStream.checksum;
  }
  
  private final int paddedLength (byte [] arr,int n)
  {
    return (arr.length + 3) & ~3;
  }

  public Set getIDs ()
  {
    return new TreeSet (tables.keySet ());
  }

  public byte [] getData (String id)
  {
    SFNTTable table = getTable (id);
    return table == null ? null : Util.toByteArray (table);
  }

  public void writeTo (DataOutput out) throws IOException
  {
    SFNTTable hmtx = getTable (HMTX);
    if (hmtx instanceof HorizontalMetrics)
      {
        HorizontalHeader hhea = (HorizontalHeader) getTable (HHEA);
        Assertions.unexpect (hhea,null);
        hhea.numOfLongHorMetrics = ((HorizontalMetrics) hmtx).advanceWidth.length;
      }

    Set ids = getIDs ();
    int ntable = ids.size ();

    SFNTChecksumStream checksumStream = new SFNTChecksumStream ();
    DataOutputMultiplexer multiplexer = new DataOutputMultiplexer
      (out,new DataOutputStream (checksumStream));

    multiplexer.writeInt (magic);
    multiplexer.writeShort (ntable);
    new BinarySearchData (ntable,unitSize).writeTo (multiplexer);
    byte [] [] data = new byte [ntable] [];
    int offset = 12 + 16 * ntable;
    int fileChecksum = 0;
    int headerIndex = -1;

    if (ids.contains (GLYF))
      ids.add (LOCA);
    else
      ids.remove (LOCA);

    Iterator iterator = ids.iterator ();
    for (int i = 0;i < ntable;i++)
      {
        String id = (String) iterator.next ();
        SFNTTable table = getTable (id);
        data [i] = Util.toByteArray (table);
        multiplexer.writeBytes (id);
        int dataChecksum = checksum (data [i]);
        fileChecksum += dataChecksum; // see below
        multiplexer.writeInt (dataChecksum);
        multiplexer.writeInt (offset);
        multiplexer.writeInt (data [i].length);
        offset += paddedLength (data [i],ntable - i);
        if (id.equals (HEAD))
          headerIndex = i;
        else if (id.equals (GLYF))
          {
            LocationTable loca = ((AbstractGlyphTable) table).getLocationTable ();
            ((SFNTHeader) getTable (HEAD)).indexToLocFormat = loca.chooseFormat ();
            addTable (loca);
          }
      }

    fileChecksum += checksumStream.checksum;
    // now we've got the correct file checksum, since we've already
    // accounted for the tables to come, see above. So we can
    // now store the checksum adjustment in the header table.
    if (headerIndex != -1)
      Util.writeInteger (0xb1b0afba - fileChecksum,data [headerIndex],8);

    for (int i = 0;i < ntable;i++)
      {
        out.write (data [i]);
        for (int j = paddedLength (data [i],ntable - i);j > data [i].length;j--)
          out.write (0);
      }
  }

  public void addTable (SFNTTable table)
  {
    tables.put (table.id,table);
  }

  public void removeTable (String id)
  {
    tables.remove (id);
  }

  public SFNTTable getTable (String id)
  {
    return (SFNTTable) tables.get (id);
  }
}
