/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.opentype;

import java.io.IOException;

import info.joriki.io.FullySeekableDataInput;

abstract public class LookupSubtable implements OffsetTable {
  int format;
  OffsetTable body;
  CoverageTable coverageTable;

  protected LookupSubtable (int format,OffsetTable body) {
    this.format = format;
    this.body = body;
    coverageTable = new CoverageTable ();
  }
  
  protected LookupSubtable (FullySeekableDataInput in) throws IOException {
    format = in.readUnsignedShort ();
    if (!((this instanceof ContextualSubstitutionSubtable ||
           this instanceof ChainingContextualSubstitutionSubtable) &&
          format == 3))
    {
      in.pushOffset (in.readUnsignedShort ());
      coverageTable = new CoverageTable (in);
      in.popOffset ();
    }
    body = readBody (in);
  }
  
  public void writeTo (OffsetOutputStream out) {
    out.writeShort (format);
    if (coverageTable != null)
      out.writeOffset (coverageTable);
    body.writeTo (out);
  }
  
  abstract protected OffsetTable readBody (FullySeekableDataInput in) throws IOException;
}
