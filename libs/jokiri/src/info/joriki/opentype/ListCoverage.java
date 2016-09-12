/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.opentype;

import info.joriki.util.Assertions;

import java.util.ArrayList;
import java.util.List;

public class ListCoverage implements Coverage {
  List glyphIDs = new ArrayList ();

  public ListCoverage() {}
  
  public ListCoverage(Coverage coverage) {
    int glyphCount = coverage.getGlyphCount ();
    for (int coverageIndex = 0;coverageIndex < glyphCount;coverageIndex++)
      addGlyphID (coverage.getGlyphID (coverageIndex));
  }

  public void writeTo(OffsetOutputStream out) {
    out.writeShort (glyphIDs.size ());
    for (int i = 0;i < glyphIDs.size ();i++)
      out.writeShort (((Integer) glyphIDs.get (i)).intValue ());
  }
  
  public int getGlyphCount () {
    return glyphIDs.size ();
  }

  public void addGlyphID (int glyphID) {
    Assertions.limit (glyphID,0,0xffff);
    glyphIDs.add (new Integer (glyphID));
  }

  public int getFormat() {
    return ARRAY_FORMAT;
  }

  public int getCoverageIndex(int glyphID) {
    for (int coverageIndex = 0;coverageIndex < glyphIDs.size ();coverageIndex++)
      if (getGlyphID (coverageIndex) == glyphID)
        return coverageIndex;
    return NOT_COVERED;
  }

  public int getGlyphID(int coverageIndex) {
    try {
      return ((Integer) glyphIDs.get (coverageIndex)).intValue ();
    } catch (IndexOutOfBoundsException ioobe) {
      return NOT_COVERED;
    }
  }
}
