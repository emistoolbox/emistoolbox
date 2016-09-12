/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.opentype;

import info.joriki.io.FullySeekableDataInput;
import info.joriki.util.Assertions;

import java.io.IOException;

public class LigatureSubstitutionSubtable extends LookupSubtable {
  public LigatureSubstitutionSubtable () {
    super (1,new LigatureSubstitution ());
  }
  
  public LigatureSubstitutionSubtable(FullySeekableDataInput in) throws IOException {
    super (in);
    Assertions.expect (((LigatureSubstitution) body).tables.size (),coverageTable.getGlyphCount ());
  }
  
  protected OffsetTable readBody (FullySeekableDataInput in) throws IOException {
    return new LigatureSubstitution (in);
  }
  
  public void addLigature (int ligatureGlyph,int [] components) {
    int [] omponents = new int [components.length - 1];
    System.arraycopy(components,1,omponents,0,omponents.length);
    LigatureSubstitution ligatureSubstitution = (LigatureSubstitution) body;
    if (!coverageTable.contains (components [0]))
    {  
      coverageTable.addGlyphID (components [0]);
      ligatureSubstitution.addTable (new LigatureSetTable ());
    }
    ((LigatureSetTable) ligatureSubstitution.getTable (coverageTable.getCoverageIndex (components [0]))).
                                             addTable (new LigatureTable (ligatureGlyph,omponents));
  }
}
