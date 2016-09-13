/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.opentype;

import java.io.DataInput;
import java.io.IOException;

public class PairSetTable implements OffsetTable {
  PairValue [] pairValues;
  
  public PairSetTable(DataInput in,PairFormat format) throws IOException {
    pairValues = new PairValue [in.readUnsignedShort ()];
    for (int i = 0;i < pairValues.length;i++)
      pairValues [i] = new PairValue (in,format);
  }

  public void writeTo(OffsetOutputStream out) {
    out.writeTables(pairValues);
  }
}
