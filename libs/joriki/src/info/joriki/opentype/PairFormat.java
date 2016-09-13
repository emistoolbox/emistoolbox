/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.opentype;

import java.io.DataInput;
import java.io.IOException;

public class PairFormat implements OffsetTable {
  int valueFormat1;
  int valueFormat2;
  
  public PairFormat (DataInput in) throws IOException {
    valueFormat1 = in.readUnsignedShort ();
    valueFormat2 = in.readUnsignedShort ();
  }
  
  public void writeTo (OffsetOutputStream out) {
    out.writeShort (valueFormat1);
    out.writeShort (valueFormat2);
  }
}
