/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.opentype;

import info.joriki.io.FullySeekableDataInput;
import info.joriki.io.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

abstract public class TaggedOpenTypeArray extends OpenTypeContainer {
  List tags = new ArrayList ();
  
  public void readEntry (FullySeekableDataInput in) throws IOException {
    String tag = Util.readString (in,4);
    tags.add (tag);
    super.readEntry (in);
  }
  
  public void writeEntry (int index,OffsetOutputStream out) {
    out.writeString ((String) tags.get (index));
    super.writeEntry (index,out);
  }

  public OffsetTable getTable(String tag) {
    int index = tags.indexOf (tag);
    return index == -1 ? null : (OffsetTable) tables.get (index);
  }
  
  public void addTable (String tag,OffsetTable table) {
    tags.add (tag);
    tables.add (table);
  }
}
