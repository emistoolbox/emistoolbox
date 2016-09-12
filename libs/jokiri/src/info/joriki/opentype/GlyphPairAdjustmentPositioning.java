/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.opentype;

import java.io.IOException;

import info.joriki.io.FullySeekableDataInput;

public class GlyphPairAdjustmentPositioning extends OpenTypeArray implements PairAdjustmentPositioning {
  PairFormat format;
  public GlyphPairAdjustmentPositioning(FullySeekableDataInput in) throws IOException {
    format = new PairFormat (in);
    readFrom (in);
  }

  public void writeTo(OffsetOutputStream out) {
    format.writeTo (out);
    super.writeTo (out);
  }

  protected OffsetTable readTable(FullySeekableDataInput in) throws IOException {
    return new PairSetTable (in,format);
  }
}
