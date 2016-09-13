/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.opentype;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OpenTypeIndices implements OffsetTable {
  List indices = new ArrayList ();
  
  public void readFrom (DataInput in) throws IOException {
    int n = in.readUnsignedShort ();
    for (int i = 0;i < n;i++)
      add (in.readUnsignedShort ());
  }
  
  public void writeTo(OffsetOutputStream out) {
    out.writeShort (indices.size ());
    for (int i = 0;i < indices.size ();i++)
      out.writeShort (((Integer) indices.get (i)).intValue ());
  }
  
  public void add (int index) {
    indices.add (new Integer (index));
  }
}
