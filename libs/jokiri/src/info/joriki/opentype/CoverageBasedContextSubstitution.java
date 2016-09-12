/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.opentype;

import java.io.IOException;

import info.joriki.io.FullySeekableDataInput;

public class CoverageBasedContextSubstitution implements OffsetTable {
  CoverageTable [] coverageTables;
  SubstitutionLookup [] lookups;
  public CoverageBasedContextSubstitution(FullySeekableDataInput in) throws IOException {
    coverageTables = new CoverageTable [in.readUnsignedShort ()];
    lookups = new SubstitutionLookup [in.readUnsignedShort()];
    for (int i = 0;i < coverageTables.length;i++)
    {
      in.pushOffset (in.readUnsignedShort ());
      coverageTables [i] = new CoverageTable (in);
      in.popOffset ();
    }
    for (int i = 0;i < lookups.length;i++)
      lookups [i] = new SubstitutionLookup (in);
  }

  public void writeTo(OffsetOutputStream out) {
    out.writeShort (coverageTables.length);
    out.writeShort (lookups.length);
    for (int i = 0;i < coverageTables.length;i++)
      out.writeOffset (coverageTables [i]);
    for (int i = 0;i < lookups.length;i++)
      lookups[i].writeTo (out);
  }
}
