/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.opentype;

import info.joriki.util.Assertions;

import java.io.DataInput;
import java.io.IOException;

public class RangeCoverage implements Coverage {
  int [] firstIDs;
  int [] lastIDs;
  
  public RangeCoverage (DataInput in) throws IOException {
    int n = in.readUnsignedShort ();
    firstIDs = new int [n];
    lastIDs = new int [n];
    int firstCoverageIndex = 0;
    for (int i = 0;i < n;i++)
    {
      firstIDs [i] = in.readShort ();
      lastIDs [i] = in.readShort ();
      Assertions.expect (in.readUnsignedShort (),firstCoverageIndex); // first coverage index
      firstCoverageIndex += rangeLength(i);
    }
  }
  
  public void writeTo(OffsetOutputStream out) {
    int n = firstIDs.length;
    Assertions.expect (lastIDs.length,n);
    out.writeShort (n);
    int firstCoverageIndex = 0;
    for (int i = 0;i < n;i++)
    {
      out.writeShort (firstIDs [i]);
      out.writeShort (lastIDs [i]);
      out.writeShort (firstCoverageIndex);
      firstCoverageIndex += rangeLength(i);
    }
  }
  
  public int getGlyphCount () {
    int glyphCount = 0;
    for (int i = 0;i < firstIDs.length;i++)
      glyphCount += rangeLength(i);
    return glyphCount;
  }

  public int getFormat() {
    return RANGE_FORMAT;
  }

  public int getCoverageIndex(int glyphID) {
    int firstCoverageIndex = 0;
    for (int i = 0;i < firstIDs.length;i++)
    {
      if (firstIDs [i] <= glyphID && glyphID <= lastIDs [i])
        return firstCoverageIndex + glyphID - firstIDs [i];
      firstCoverageIndex += rangeLength(i);
    }
    return NOT_COVERED;
  }

  public int getGlyphID(int coverageIndex) {
    int firstCoverageIndex = 0;
    for (int i = 0;i < firstIDs.length;i++)
    {
      int lastCoverageIndex = firstCoverageIndex + lastIDs [i] - firstIDs [i];
      if (firstCoverageIndex <= coverageIndex && coverageIndex <= lastCoverageIndex)
        return firstIDs [i] + coverageIndex - firstCoverageIndex;
      firstCoverageIndex = lastCoverageIndex + 1;
    }
    return NOT_COVERED;
  }

  final private int rangeLength(int i) {
    return (lastIDs [i] - firstIDs [i]) + 1;
  }
}
