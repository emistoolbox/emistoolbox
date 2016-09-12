/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.sfnt;

import java.io.DataInput;
import java.io.IOException;
import java.io.DataOutput;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import info.joriki.util.Range;
import info.joriki.util.Assertions;

public class ByteEncodingTable extends CMapTable
{
  public byte [] glyphIndices = new byte [256];

  public ByteEncodingTable (DataInput in) throws IOException
  {
    int length = in.readUnsignedShort ();
    Assertions.expect (length,0x106);
    language = in.readShort ();
    in.readFully (glyphIndices);
  }

  public ByteEncodingTable (CMapTable table)
  {
    super (table);

    Iterator iterator = table.pairIterator ();
    while (iterator.hasNext ())
      {
        EncodingPair pair = (EncodingPair) iterator.next ();
        Assertions.expect (pair.glyphIndex & 0xff,pair.glyphIndex);
        glyphIndices [pair.code] = (byte) pair.glyphIndex;
      }
  }

  public ByteEncodingTable (int platform,int encoding,int language)
  {
    super (platform,encoding,language);
  }

  public void writeTo (DataOutput out) throws IOException
  {
    out.writeShort (0);
    out.writeShort (0x106);
    out.writeShort (0);
    out.write (glyphIndices);
  }

  public void map (int code,int glyphIndex)
  {
    glyphIndices [code] = (byte) glyphIndex;
  }

  public ByteEncodingTable toByteEncodingTable ()
  {
    return this;
  }

  public Map getMap ()
  {
    Map map = new HashMap ();
    for (char i = 0;i < glyphIndices.length;i++)
      if (glyphIndices [i] != 0)
        map.put (new Character (i),new Integer (glyphIndices [i] & 0xff));
    return map;
  }

  public int getGlyphIndex (int code)
  {
    return glyphIndices [code] & 0xff;
  }

  public Range getDomain ()
  {
    return new Range (0,0xff);
  }
}
