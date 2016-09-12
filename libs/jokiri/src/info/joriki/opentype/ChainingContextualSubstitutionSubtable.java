/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.opentype;

import java.io.IOException;

import info.joriki.io.FullySeekableDataInput;
import info.joriki.util.NotImplementedException;

public class ChainingContextualSubstitutionSubtable extends LookupSubtable {
  public ChainingContextualSubstitutionSubtable(FullySeekableDataInput in) throws IOException {
    super(in);
  }

  protected OffsetTable readBody (FullySeekableDataInput in) throws IOException {
    switch (format) {
    case 3: return new CoverageBasedChainingContextSubstitution (in);
    default: throw new NotImplementedException ("chaining contextual substitution subtable format " + format);
    }
  }
}
