/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.sfnt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import info.joriki.util.Range;
import info.joriki.util.General;
import info.joriki.util.Options;
import info.joriki.util.Assertions;

public class SegmentMappingTable extends CMapTable
{
  static class CodeRange implements Comparable
  {
    char  startCode;
    char  endCode;
    short idDelta;
    int idRangeOffset;

    public int compareTo (Object o)
    {
      return endCode - ((CodeRange) o).endCode;
    }
  }

  CodeRange [] ranges;
  char [] glyphIdArray;

  SegmentMappingTable (DataInput in) throws IOException
  {
    // don't use this length value, for two reasons:
    // an Asian font I found in a PDF file had this value
    // too high by 6. Also, the freetype source says that
    // the language field has been turned to 4 bytes on the
    // Mac and doesn't mention the length field.
    in.readUnsignedShort (); // iffy length
    language = in.readShort ();
    int segCount = in.readUnsignedShort () / 2;
    new BinarySearchData (segCount,2).assertFrom (in);
    ranges = new CodeRange [segCount];
    for (int j = 0;j < segCount;j++)
      ranges [j] = new CodeRange ();
    for (int j = 0;j < segCount;j++)
      ranges [j].endCode = in.readChar ();
    int reserved = in.readUnsignedShort ();
    if (reserved != 0)
      Options.warn ("non-zero reserved field in segment mapping table");
    for (int j = 0;j < segCount;j++)
      ranges [j].startCode = in.readChar ();
    for (int j = 0;j < segCount;j++)
      ranges [j].idDelta = in.readShort ();
    for (int j = 0;j < segCount;j++)
      ranges [j].idRangeOffset = in.readUnsignedShort ();

    // annoyingly, we need to figure out for ourselves
    // how long glyphIdArray actually is. We can't use
    // iffyLength (see above), and we can't get a length
    // from the CMap since that can apparently have unordered
    // offsets and even reuses tables for various platform/
    // encoding combinations, so subtracting offsets there
    // won't help. So there's no way to know how many glyphIds
    // there are other than going through the motions of
    // extracting them; this is taken from getMap with the
    // irrelevant parts removed

    int glyphIdArrayLength = 0;
    for (int j = 0;j < ranges.length - 1;j++)
      {
        CodeRange range = ranges [j];
        int index = range.idRangeOffset / 2 + j - ranges.length;
        for (char k = range.startCode;k <= range.endCode;k++)
          if (range.idRangeOffset != 0)
            glyphIdArrayLength = Math.max (glyphIdArrayLength,++index);
      }
    glyphIdArray = new char [glyphIdArrayLength];
    for (int j = 0;j < glyphIdArray.length;j++)
      glyphIdArray [j] = in.readChar ();
  }

  public SegmentMappingTable (CMapTable table)
  {
    this (table.getMap (),table.platform,table.encoding,table.language);
  }

  public SegmentMappingTable
    (Map pairMap,int platform,int encoding,int language)
  {
    this (new EncodingPairIterator (pairMap),platform,encoding,language);
  }

  public SegmentMappingTable
    (Iterator pairIterator,int platform,int encoding,int language)
  {
    super (platform,encoding,language);

    char [] glyphIndices = new char [0x10000];
    int max = 0;
    while (pairIterator.hasNext ())
      {
        EncodingPair pair = (EncodingPair) pairIterator.next ();
        glyphIndices [pair.code] = (char) pair.glyphIndex;
        max = Math.max (max,pair.code);
      }

    List glyphList = new ArrayList ();
    List rangeList = new ArrayList ();
    List deltaList = new ArrayList ();

    for (int k = 0;;)
      {
        while (k <= max && glyphIndices [k] == 0)
          k++;
        if (k > max)
          break;
        CodeRange range = new CodeRange ();
        range.startCode = (char) k;
        range.idDelta = (short) (glyphIndices [k] - k);
        range.idRangeOffset = glyphList.size () - rangeList.size ();
        boolean oneDelta = true;
        while (glyphIndices [++k] != 0)
          oneDelta &= glyphIndices [k] - k == range.idDelta;
        range.endCode = (char) (k - 1);
        if (oneDelta)
          range.idRangeOffset = 0;
        else
          {
            range.idDelta = 0;
            for (int i = range.startCode;i <= range.endCode;i++)
              glyphList.add (new Character (glyphIndices [i]));
          }
        rangeList.add (range);
        deltaList.add (new Boolean (oneDelta));
      }
    
    CodeRange dummyRange = new CodeRange ();
    dummyRange.startCode = dummyRange.endCode = 0xffff;
    dummyRange.idDelta = 1;
    rangeList.add (dummyRange);
    deltaList.add (new Boolean (true));

    ranges = new CodeRange [rangeList.size ()];

    for (int i = 0;i < ranges.length;i++)
      {
        CodeRange range = ranges [i] = (CodeRange) rangeList.get (i);
        boolean oneDelta = ((Boolean) deltaList.get (i)).booleanValue ();
        if (oneDelta)
          range.idRangeOffset = 0;
        else
          {
            range.idDelta = 0;
            ranges [i].idRangeOffset += ranges.length;
            ranges [i].idRangeOffset *= 2;
          }
      }
    
    glyphIdArray = General.toCharArray (glyphList);
  }

  public void writeTo (DataOutput out) throws IOException
  {
    out.writeShort (4);
    out.writeShort (16 + 8 * ranges.length + 2 * glyphIdArray.length);
    out.writeShort (0);
    out.writeShort (ranges.length * 2);
    new BinarySearchData (ranges.length,2).writeTo (out);
    for (int j = 0;j < ranges.length;j++)
      out.writeChar  (ranges [j].endCode);
    out.writeShort (0);
    for (int j = 0;j < ranges.length;j++)
      out.writeChar  (ranges [j].startCode);
    for (int j = 0;j < ranges.length;j++)
      out.writeShort (ranges [j].idDelta);
    for (int j = 0;j < ranges.length;j++)
      out.writeShort (ranges [j].idRangeOffset);
    for (int j = 0;j < glyphIdArray.length;j++)
      out.writeShort (glyphIdArray [j]);
  }

  public Map getMap ()
  {
    Map map = new HashMap ();
    for (int j = 0;j < ranges.length - 1;j++)
      {
        CodeRange range = ranges [j];
        int index = range.idRangeOffset / 2 + j - ranges.length;
        int delta = range.idDelta;
        for (char k = range.startCode;k <= range.endCode;k++)
          {
            char id;
            if (range.idRangeOffset == 0)
              id = (char) (k + delta);
            else
              {
                id = glyphIdArray [index++];
                if (id != 0)
                  id += delta;
              }
    
            map.put (new Integer (k),new Integer (id));
          }
      }
    return map;
  }

  public static boolean overflow = false;

  public int getGlyphIndex (int code)
  {
    for (int i = 0;i < ranges.length;i++) {
      CodeRange range = ranges [i];
      if (range.endCode >= code) {
        if (range.startCode > code)
          return 0; // missing glyph
        if (range.idRangeOffset == 0)
          return overflow ? code + range.idDelta : (code + range.idDelta) & 0xffff;
        int index = glyphIdArray
        [range.idRangeOffset / 2 +
         i - ranges.length + 
         code - range.startCode];
        return index == 0 ? 0 : (index + range.idDelta) & 0xffff;
      }
    }
    Assertions.expect (code > 0xffff);
    return 0;
  }

  public Range getDomain ()
  {
    int beg = Integer.MAX_VALUE;
    int end = Integer.MIN_VALUE;
    for (int i = 0;i < ranges.length;i++)
      {
	CodeRange range = ranges [i];
	beg = Math.min (beg,range.startCode);
	// 1575056801.pdf contains a cmap that maps only 0xffff
	if (range.endCode != 0xffff || ranges.length == 1)
	  end = Math.max (end,range.endCode);
      }
    Assertions.expect (beg <= end);
    return new Range (beg,end);
  }
}
