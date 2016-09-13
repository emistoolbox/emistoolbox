/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.opentype;

import java.io.IOException;

import info.joriki.io.FullySeekableDataInput;

public class AlternateSetTable extends OpenTypeIndices {
  public AlternateSetTable(FullySeekableDataInput in) throws IOException {
    readFrom (in);
  }
}
