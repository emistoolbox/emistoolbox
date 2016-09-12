/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.opentype;

import info.joriki.io.FullySeekableDataInput;

import java.io.IOException;

public class AlternateSubstitutionSubtable extends LookupSubtable {
  public AlternateSubstitutionSubtable(FullySeekableDataInput in) throws IOException {
    super (in);
  }
  
  protected OffsetTable readBody(FullySeekableDataInput in) throws IOException {
    return new AlternateSubstitution (in);
  }
}
