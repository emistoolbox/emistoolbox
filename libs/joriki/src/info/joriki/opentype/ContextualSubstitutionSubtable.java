/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.opentype;

import info.joriki.io.FullySeekableDataInput;
import info.joriki.util.NotImplementedException;

import java.io.IOException;

public class ContextualSubstitutionSubtable extends LookupSubtable {
  public ContextualSubstitutionSubtable(FullySeekableDataInput in) throws IOException {
    super(in);
  }
  
  protected OffsetTable readBody (FullySeekableDataInput in) throws IOException {
    switch (format) {
    case 2: return new ClassBasedContextSubstitution (in);
    case 3: return new CoverageBasedContextSubstitution (in);
    default: throw new NotImplementedException ("contextual substitution subtable format " + format);
    }
  }
}