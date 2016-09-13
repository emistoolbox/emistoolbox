/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.opentype;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import info.joriki.io.FullySeekableDataInput;

abstract public class OpenTypeContainer implements OffsetTable {
  List tables = new ArrayList ();
  
  public void readFrom (FullySeekableDataInput in) throws IOException {
    int n = in.readUnsignedShort ();
    for (int i = 0;i < n;i++)
      readEntry (in);
  }
  
  public void writeTo (OffsetOutputStream out) {
    int n = tables.size ();
    out.writeShort (n);
    for (int i = 0;i < n;i++)
      writeEntry (i,out);
  }

  protected void readEntry (FullySeekableDataInput in) throws IOException {
    int offset = in.readUnsignedShort ();
    if (offset == 0) // e.g. in class-based context substitution
      tables.add (null);
    else
    {
      in.pushOffset (offset);
      tables.add(readTable (in));
      in.popOffset ();
    }
  }
  
  protected void writeEntry (int index,OffsetOutputStream out) {
    OffsetTable table = (OffsetTable) tables.get (index);
    if (table == null)
      out.writeShort (0);
    else
      out.writeOffset (table);
  }
  
  abstract protected OffsetTable readTable (FullySeekableDataInput in) throws IOException;
}
