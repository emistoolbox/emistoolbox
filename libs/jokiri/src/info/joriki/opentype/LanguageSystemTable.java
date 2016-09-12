/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.opentype;

import java.io.DataInput;
import java.io.IOException;

import info.joriki.util.Assertions;

public class LanguageSystemTable extends OpenTypeIndices {
  int requiredFeatureIndex;
  
  public LanguageSystemTable () {
    requiredFeatureIndex = 0xffff;
  }
  
  public LanguageSystemTable (DataInput in) throws IOException {
    Assertions.expect (in.readUnsignedShort (),0); // reserved for an offset to a reordering table
    requiredFeatureIndex = in.readUnsignedShort ();
    readFrom (in);
  }
  
  public void writeTo (OffsetOutputStream out) {
    out.writeShort (0); // reserved for an offset to a reordering table
    out.writeShort (requiredFeatureIndex);
    super.writeTo (out);
  }
}
