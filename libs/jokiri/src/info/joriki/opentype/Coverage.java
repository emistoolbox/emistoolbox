/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.opentype;

public interface Coverage extends OffsetTable {
  int ARRAY_FORMAT = 1;
  int RANGE_FORMAT = 2;
  int NOT_COVERED = -1;

  int getFormat();
  int getGlyphCount ();
  int getCoverageIndex(int glyphID);
  int getGlyphID(int coverageIndex);
}
