/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.opentype;

import info.joriki.io.FullySeekableDataInput;

import java.io.IOException;

public class ClassBasedContextSubstitution extends OpenTypeArray implements ContextSubstitution {
  ClassDefinitionTable classDefinitionTable;
  public ClassBasedContextSubstitution (FullySeekableDataInput in) throws IOException {
    in.pushOffset (in.readUnsignedShort ());
    classDefinitionTable = new ClassDefinitionTable (in);
    in.popOffset ();
    readFrom (in);
  }

  public void writeTo(OffsetOutputStream out) {
    out.writeOffset (classDefinitionTable);
    super.writeTo (out);
  }

  protected OffsetTable readTable(FullySeekableDataInput in) throws IOException {
    return new SubClassSetTable (in);
  }
}
