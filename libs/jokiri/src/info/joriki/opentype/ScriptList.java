/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.opentype;

import info.joriki.io.FullySeekableDataInput;

import java.io.IOException;

public class ScriptList extends TaggedOpenTypeArray {
  public static class ScriptTable extends TaggedOpenTypeArray {
    LanguageSystemTable defaultLanguageSystemTable;
    
    ScriptTable () {}
    
    ScriptTable(FullySeekableDataInput in) throws IOException {
      int defaultOffset = in.readUnsignedShort ();
      if (defaultOffset != 0)
      {  
        in.pushOffset(defaultOffset);
        defaultLanguageSystemTable = new LanguageSystemTable (in);
        in.popOffset ();
      }
      readFrom (in);
    }

    protected OffsetTable readTable(FullySeekableDataInput in) throws IOException {
      return new LanguageSystemTable (in);
    }
    
    public void writeTo (OffsetOutputStream out) {
      if (defaultLanguageSystemTable == null)
        out.writeShort (0);
      else
        out.writeOffset (defaultLanguageSystemTable);
      super.writeTo (out);
    }
  }
  
  protected OffsetTable readTable (FullySeekableDataInput in) throws IOException {
    return new ScriptTable (in);
  }
}
