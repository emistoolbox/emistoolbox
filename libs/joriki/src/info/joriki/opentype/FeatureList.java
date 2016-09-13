/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.opentype;

import info.joriki.io.FullySeekableDataInput;
import info.joriki.util.NotImplementedException;

import java.io.DataInput;
import java.io.IOException;

public class FeatureList extends TaggedOpenTypeArray {
  static class FeatureTable extends OpenTypeIndices {
    FeatureTable () {}
    
    FeatureTable (DataInput in) throws IOException {
      if (in.readUnsignedShort () != 0)
        throw new NotImplementedException ("feature parameters");
      readFrom (in);
    }
    
    public void writeTo(OffsetOutputStream out) {
      out.writeShort (0); // no feature parameters
      super.writeTo (out);
    }
  }
  
  protected OffsetTable readTable (FullySeekableDataInput in) throws IOException {
    return new FeatureTable (in);
  }
}
