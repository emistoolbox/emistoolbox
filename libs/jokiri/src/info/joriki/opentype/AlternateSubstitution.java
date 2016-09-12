/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.opentype;

import java.io.IOException;

import info.joriki.io.FullySeekableDataInput;

public class AlternateSubstitution extends OpenTypeArray {
  public AlternateSubstitution(FullySeekableDataInput in) throws IOException {
    readFrom (in);
  }

  protected OffsetTable readTable(FullySeekableDataInput in) throws IOException {
    return new AlternateSetTable (in);
  }
}
