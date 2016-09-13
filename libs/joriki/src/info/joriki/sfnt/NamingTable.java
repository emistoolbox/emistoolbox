/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.sfnt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import java.util.List;

import info.joriki.io.Util;
import info.joriki.io.SeekableByteArray;
import info.joriki.util.General;
import info.joriki.util.Assertions;

public class NamingTable extends EncodingDependentTable
{
  int format;

  static class NameRecord extends EncodingScheme
  {
    short name;
    byte [] string;

    NameRecord (int platform,int encoding,int language,int name)
    {
      super (platform,encoding,language);
      this.name = (short) name;
    }

    NameRecord (DataInput in) throws IOException
    {
      platform = in.readShort ();
      encoding = in.readShort ();
      language = in.readShort ();
      name     = in.readShort ();
    }

    void writeTo (DataOutput out) throws IOException
    {
      out.writeShort (platform);
      out.writeShort (encoding);
      out.writeShort (language);
      out.writeShort (name);
    }

    public int compareTo (Object o)
    {
      int result = super.compareTo (o);
      return result == 0 ? name - ((NameRecord) o).name : result;
    }

    public String toString ()
    {
      return super.toString () + "/" + name + " : \"" +new String (string) + "\"";
    }
  }

  public NamingTable (DataInput in,int size) throws IOException
  {
    super (NAME);

    format = in.readUnsignedShort ();
    Assertions.expect (format,0);
    int nrecord = in.readUnsignedShort ();
    NameRecord [] nameRecords = new NameRecord [nrecord];
    int start = in.readUnsignedShort ();
    Assertions.expect (start,6 + nrecord * 12);

    int [] length = new int [nrecord];
    int [] offset = new int [nrecord];

    for (int i = 0;i < nrecord;i++)
      {
        nameRecords [i] = new NameRecord (in);
        length [i] = in.readUnsignedShort ();
        offset [i] = in.readUnsignedShort ();
      }

    SeekableByteArray stringData =
      new SeekableByteArray (Util.readBytes (in,size - start));
    for (int i = 0;i < nrecord;i++)
      {
        stringData.seek (offset [i]);
        NameRecord nameRecord = nameRecords [i];
        nameRecord.string = Util.readBytes (stringData,length [i]);
        addEntry (nameRecord);
      }
  }

  public void writeTo (DataOutput out) throws IOException
  {
    int nrecord = entries.size ();

    out.writeShort (format);
    out.writeShort (nrecord);
    out.writeShort (6 + nrecord * 12);

    DataBlockList dataBlocks = new DataBlockList (); 

    Collection values = entries.values ();
    Iterator iterator = values.iterator ();
    while (iterator.hasNext ())
      {
        NameRecord nameRecord = (NameRecord) iterator.next ();
        nameRecord.writeTo (out);
        DataBlock dataBlock = new DataBlock (nameRecord.string);
        dataBlocks.add (dataBlock);
        out.writeShort (dataBlock.data.length);
        out.writeShort (dataBlock.offset);
      }

    for (DataBlock dataBlock : dataBlocks)
        out.write (dataBlock.data);
  }

  byte [] getName (int platform,int encoding,int language,int name)
  {
    NameRecord nameRecord = (NameRecord) get
      (new NameRecord (platform,encoding,language,name));
    return nameRecord == null ? null : nameRecord.string;
  }

  String getMacName (int name)
  {
    try {
      byte [] bytes = getName (1,0,0,name);
      return bytes == null ? null : new String (bytes,"MacRoman");
    } catch (UnsupportedEncodingException uee) {
      uee.printStackTrace ();
      return null;
    }
  }

  final static int standardLanguage = 0x409;

  String getMicrosoftName (int encoding,int name)
  {
    byte [] bytes = getName (3,encoding,standardLanguage,name);
    return bytes == null ? null : new String (General.packIntoChars (bytes));
  }

  String getMicrosoftName (int name)
  {
    String str = getMicrosoftName (1,name);
    if (str == null)
      str = getMicrosoftName (0,name);
    return str;
  }

  public String getName (int name)
  {
    String str = getMacName (name);
    if (str == null)
      str = getMicrosoftName (name);
    return str;
  }

  public String getPostScriptName ()
  {
    String macRoman = getMacName (6);
    String microsoft = getMicrosoftName (6);
    Assertions.expect (microsoft,macRoman);
    return macRoman;
  }

  public String getCopyrightNotice ()
  {
    return getName (0);
  }

  public String getTradeMark ()
  {
    return getName (7);
  }

  public String getSubfamily ()
  {
    return getName (2);
  }
}
