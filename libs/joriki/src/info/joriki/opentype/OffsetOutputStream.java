/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.opentype;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Stack;

import info.joriki.io.WriteableByteArray;

// potential size optimization: make use of identical blocks

public class OffsetOutputStream extends WriteableByteArray {
  int offset;
  List list;
  Stack listStack;
  java.util.Set byteArrays = new java.util.HashSet ();

  public void writeShort (int value) {
    write (value >> 8);
    write (value);
  }
  
  public void writeString (String string) {
    write (string.getBytes ());
  }

  public void writeOffset (OffsetTable table) {
    list.add (new Integer (count));
    list.add (table);
    write (0);
    write (0);
  }
  
  public void writeTable (OffsetTable table) {
    int origin = count;
    list = new ArrayList ();
    table.writeTo (this);
    Iterator listIterator = list.iterator ();
    while (listIterator.hasNext ()) {
      int index = ((Integer) listIterator.next ()).intValue ();
      int offset = count - origin;
      buf [index]     = (byte) (offset >> 8);
      buf [index + 1] = (byte) offset;
      writeTable (((OffsetTable) listIterator.next ()));
    }
  }

  public void writeTables (OffsetTable [] tables) {
    writeShort (tables.length);
    for (int i = 0;i < tables.length;i++)
      tables [i].writeTo (this);
  }
  
  public void writeOffsets (OffsetTable [] tables) {
    writeShort (tables.length);
    for (int i = 0;i < tables.length;i++)
      writeOffset (tables [i]);
  }
}
