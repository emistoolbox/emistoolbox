/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.opentype;

abstract public class OpenTypeArray extends OpenTypeContainer {
  public void addTable(OffsetTable table) {
    tables.add (table);
  }

  public OffsetTable getTable(int i) {
    return (OffsetTable) tables.get (i);
  }  
}
