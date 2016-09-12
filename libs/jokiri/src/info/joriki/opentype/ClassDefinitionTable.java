/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.opentype;

import java.io.DataInput;
import java.io.IOException;

import info.joriki.util.NotImplementedException;

public class ClassDefinitionTable implements OffsetTable {
  int format;
  ClassDefinition classDefinition;
  
  public ClassDefinitionTable (DataInput in) throws IOException {
    format = in.readUnsignedShort ();
    switch (format) {
    case 2 :
      classDefinition = new RangeClassDefinition (in);
      break;
    default: throw new NotImplementedException ("class definition table format " + format);
    }
  }

  public void writeTo(OffsetOutputStream out) {
    out.writeShort (format);
    classDefinition.writeTo (out);
  }
}
