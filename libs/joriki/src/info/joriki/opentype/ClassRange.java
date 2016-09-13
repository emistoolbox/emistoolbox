/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.opentype;

import java.io.DataInput;
import java.io.IOException;

public class ClassRange implements OffsetTable {
  int firstGlyph;
  int lastGlyph;
  int glyphClass;
  
  public ClassRange (DataInput in) throws IOException {
    firstGlyph = in.readUnsignedShort ();
    lastGlyph = in.readUnsignedShort ();
    glyphClass = in.readUnsignedShort ();
  }

  public void writeTo(OffsetOutputStream out) {
    out.writeShort (firstGlyph);
    out.writeShort (lastGlyph);
    out.writeShort (glyphClass);
  }
}
