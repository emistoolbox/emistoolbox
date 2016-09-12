/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.opentype;

import java.io.DataInput;
import java.io.IOException;

public class ArrayCoverage implements Coverage {
  short [] glyphIDs;
  
  public ArrayCoverage (DataInput in) throws IOException {
    glyphIDs = new short [in.readUnsignedShort ()];
    for (int i = 0;i < glyphIDs.length;i++)
      glyphIDs [i] = in.readShort ();
  }
  
  public void writeTo(OffsetOutputStream out) {
    out.writeShort (glyphIDs.length);
    for (int i = 0;i < glyphIDs.length;i++)
      out.writeShort (glyphIDs [i]);
  }
  
  public int getGlyphCount () {
    return glyphIDs.length;
  }

  public int getFormat() {
    return ARRAY_FORMAT;
  }

  public int getCoverageIndex(int glyphID) {
    short signed = (short) glyphID;
    for (int coverageIndex = 0;coverageIndex < glyphIDs.length;coverageIndex++)
      if (glyphIDs [coverageIndex] == signed)
        return coverageIndex;
    return NOT_COVERED;
  }

  public int getGlyphID(int coverageIndex) {
    return (0 <= coverageIndex && coverageIndex < glyphIDs.length) ? glyphIDs [coverageIndex] & 0xffff : NOT_COVERED;
  }
}
