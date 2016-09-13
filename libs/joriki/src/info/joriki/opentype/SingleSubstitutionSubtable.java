/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.opentype;

import info.joriki.io.FullySeekableDataInput;
import info.joriki.util.NotImplementedException;

import java.io.IOException;

public class SingleSubstitutionSubtable extends LookupSubtable {
  public SingleSubstitutionSubtable(FullySeekableDataInput in) throws IOException {
    super(in);
  }
  
  protected OffsetTable readBody (FullySeekableDataInput in) throws IOException {
    switch (format) {
    case 2: return new SpecifiedSingleSubstitution (in);
    default: throw new NotImplementedException ("single substitution subtable format " + format);
    }
  }
}
