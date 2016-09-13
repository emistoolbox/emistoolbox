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

import info.joriki.util.Range;
import info.joriki.util.InfiniteArrayList;

public class TrimmedTable extends CMapTable
{
  char firstCode;
  InfiniteArrayList glyphIdArray = new InfiniteArrayList ();

  public TrimmedTable (int platform,int encoding,int language)
  {
    this (platform,encoding,language,(char) 0);
  }

  public TrimmedTable (int platform,int encoding,int language,char firstCode)
  {
    super (platform,encoding,language);
    this.firstCode = firstCode;
  }

  public TrimmedTable (DataInput in) throws IOException
  {
    in.readUnsignedShort (); // length
    language = in.readShort ();
    firstCode = in.readChar ();
    int entryCount = in.readUnsignedShort ();
    for (int i = 0;i < entryCount;i++)
      glyphIdArray.set (i,new Integer (in.readUnsignedShort ()));
  }

  public void writeTo (DataOutput out) throws IOException
  {
    out.writeShort (6);
    out.writeShort (2 * (5 + glyphIdArray.size ()));
    out.writeShort (language);
    out.writeShort (firstCode);
    out.writeShort (glyphIdArray.size ());
    for (int i = 0;i < glyphIdArray.size ();i++)
      {
        Integer id = (Integer) glyphIdArray.get (i);
        out.writeShort (id != null ? id.intValue () : 0);
      }
  }

  public void map (char code,int glyphIndex)
  {
    glyphIdArray.set (code - firstCode,new Integer (glyphIndex));
  }

  public int getGlyphIndex (int code)
  {
    Integer index = (Integer) glyphIdArray.get (code - firstCode);
    return index != null ? index.intValue () : 0;
  }

  public Map getMap ()
  {
    Map map = new HashMap ();
    for (char i = 0;i < glyphIdArray.size ();i++)
      {
        Object glyphId = glyphIdArray.get (i);
        if (glyphId != null)
          map.put (new Character ((char) (firstCode + i)),glyphId);
      }
    return map;
  }

  public Range getDomain ()
  {
    return new Range (0,glyphIdArray.size () - 1);
  }
}
