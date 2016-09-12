/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.opentype;

import info.joriki.io.FullySeekableDataInput;
import info.joriki.util.NotImplementedException;

import java.io.IOException;

public class PairAdjustmentPositioningSubtable extends LookupSubtable {
  public PairAdjustmentPositioningSubtable (FullySeekableDataInput in) throws IOException {
    super (in);
  }
  
  protected OffsetTable readBody(FullySeekableDataInput in) throws IOException {
    switch (format) {
    case 1: return new GlyphPairAdjustmentPositioning (in);
    default: throw new NotImplementedException ("pair adjustment positioning subtable format " + format);
    }
  }
}
