/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.opentype;

public interface OffsetTable {
  void writeTo (OffsetOutputStream out);
}
