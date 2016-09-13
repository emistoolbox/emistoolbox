/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.opentype;

import java.io.DataInput;
import java.io.IOException;

import info.joriki.util.NotImplementedException;

public class CoverageTable implements OffsetTable {
  int format;
  Coverage coverage;

  public CoverageTable () {
    coverage = new ListCoverage ();
  }
  
  public CoverageTable (DataInput in) throws IOException {
    format = in.readUnsignedShort ();
    switch (format) {
    case 1:
      coverage = new ArrayCoverage (in);
      break;
    case 2:
      coverage = new RangeCoverage (in);
      break;
    default:
      throw new NotImplementedException ("coverage table format " + format);    
    }
  }

  public void writeTo (OffsetOutputStream out) {
    out.writeShort (coverage.getFormat ());
    coverage.writeTo (out);
  }

  public boolean contains(int glyphID) {
    return coverage.getCoverageIndex (glyphID) != Coverage.NOT_COVERED;
  }

  public int getGlyphCount() {
    return coverage.getGlyphCount ();
  }
  
  public int getCoverageIndex(int glyphID) {
    return coverage.getCoverageIndex (glyphID);
  }

  public void addGlyphID(int glyphID) {
    if (!(coverage instanceof ListCoverage))
      coverage = new ListCoverage (coverage);
    ((ListCoverage) coverage).addGlyphID (glyphID);
  }
}
