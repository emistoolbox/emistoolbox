/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.opentype;

import info.joriki.io.FullySeekableDataInput;
import info.joriki.util.NotImplementedException;

import java.io.IOException;

public class GlyphSubstitutionTable extends GlyphProcessingTable {
  public GlyphSubstitutionTable() {
    super (GSUB);
  }

  public GlyphSubstitutionTable(FullySeekableDataInput in) throws IOException {
    super(GSUB,in);
  }

  protected LookupSubtable readSubtable (FullySeekableDataInput in,int type) throws IOException {
    switch (type) {
    case 1 : return new SingleSubstitutionSubtable (in);
    case 3 : return new AlternateSubstitutionSubtable (in);
    case 4 : return new LigatureSubstitutionSubtable (in);
    case 5 : return new ContextualSubstitutionSubtable (in);
    case 6 : return new ChainingContextualSubstitutionSubtable (in);
    default: throw new NotImplementedException ("lookup subtable type " + type + " in " + id);
    }
  }
}
