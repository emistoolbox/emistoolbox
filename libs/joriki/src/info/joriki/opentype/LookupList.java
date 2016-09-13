/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.opentype;

import java.io.IOException;

import info.joriki.io.FullySeekableDataInput;

public class LookupList extends OpenTypeArray {
  GlyphProcessingTable glyphProcessingTable;
  
  class LookupTable extends OpenTypeArray {
    int type;
    int flags;

    LookupTable (int type,int flags) {
      this.type = type;
      this.flags = flags;
    }
    
    LookupTable (FullySeekableDataInput in) throws IOException {
      type = in.readUnsignedShort ();
      flags = in.readUnsignedShort ();
      readFrom (in);
    }
    
    public void writeTo (OffsetOutputStream out) {
      out.writeShort (type);
      out.writeShort (flags);
      super.writeTo (out);
    }

    protected OffsetTable readTable(FullySeekableDataInput in) throws IOException {
      return glyphProcessingTable.readSubtable (in,type);
    }
  }
  
  public LookupList(GlyphProcessingTable glyphProcessingTable) {
    this.glyphProcessingTable = glyphProcessingTable;
  }
  
  public OffsetTable readTable (FullySeekableDataInput in) throws IOException {
    return new LookupTable (in);
  }
}
