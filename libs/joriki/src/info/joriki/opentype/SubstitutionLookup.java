/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.opentype;

import java.io.DataInput;
import java.io.IOException;

public class SubstitutionLookup implements OffsetTable {
  int sequenceIndex;
  int lookupListIndex;
  
  public SubstitutionLookup (DataInput in) throws IOException {
    sequenceIndex = in.readUnsignedShort ();
    lookupListIndex = in.readUnsignedShort ();
  }
  
  public void writeTo (OffsetOutputStream out) {
    out.writeShort (sequenceIndex);
    out.writeShort (lookupListIndex);
  }
}
