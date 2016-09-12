/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.opentype;

import info.joriki.io.FullySeekableDataInput;

import java.io.IOException;

public class LigatureSubstitution extends OpenTypeArray {
  public LigatureSubstitution () {}
  
  public LigatureSubstitution (FullySeekableDataInput in) throws IOException {
    readFrom (in);
  }

  protected OffsetTable readTable(FullySeekableDataInput in) throws IOException {
    return new LigatureSetTable (in);
  }
}
