/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.opentype;

import java.io.DataInput;
import java.io.IOException;

public class RangeClassDefinition implements ClassDefinition {
  ClassRange [] ranges;
  
  public RangeClassDefinition (DataInput in) throws IOException {
    ranges = new ClassRange [in.readUnsignedShort ()];
    for (int i = 0;i < ranges.length;i++)
      ranges [i] = new ClassRange (in);
  }
  
  public void writeTo(OffsetOutputStream out) {
    out.writeTables (ranges);
  }
}
