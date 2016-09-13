/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.opentype;

import java.io.IOException;

import info.joriki.io.FullySeekableDataInput;

public class CoverageBasedChainingContextSubstitution implements ChainingContextSubsitution {
  CoverageTable [] backtrackCoverageTables;
  CoverageTable []     inputCoverageTables;
  CoverageTable [] lookaheadCoverageTables;
  SubstitutionLookup [] lookups;
  
  public CoverageBasedChainingContextSubstitution(FullySeekableDataInput in) throws IOException {
    backtrackCoverageTables = readCoverageTables (in);
        inputCoverageTables = readCoverageTables (in);
    lookaheadCoverageTables = readCoverageTables (in);
    lookups = new SubstitutionLookup [in.readUnsignedShort ()];
    for (int i = 0;i < lookups.length;i++)
      lookups [i] = new SubstitutionLookup (in);
  }

  public void writeTo(OffsetOutputStream out) {
    out.writeOffsets (backtrackCoverageTables);
    out.writeOffsets (    inputCoverageTables);
    out.writeOffsets (lookaheadCoverageTables);
    out.writeTables  (lookups);
  }
  
  private CoverageTable[] readCoverageTables (FullySeekableDataInput in) throws IOException {
    CoverageTable [] coverageTables = new CoverageTable [in.readUnsignedShort()];
    for (int i = 0;i < coverageTables.length;i++) {
      in.pushOffset (in.readUnsignedShort ());
      coverageTables [i] = new CoverageTable (in);
      in.popOffset ();
    }
    return coverageTables;
  }
}
