/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.opentype;

import info.joriki.io.FullySeekableDataInput;
import info.joriki.util.NotImplementedException;

import java.io.IOException;

public class GlyphPositioningTable extends GlyphProcessingTable {
  public GlyphPositioningTable(FullySeekableDataInput in) throws IOException {
    super(GPOS,in);
  }
  
  protected LookupSubtable readSubtable (FullySeekableDataInput in,int type) throws IOException {
    switch (type) {
    case 2: return new PairAdjustmentPositioningSubtable (in);
    default: throw new NotImplementedException ("lookup subtable type " + type + " in " + id);
    }
  }
}
