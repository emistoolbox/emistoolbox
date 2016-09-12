/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.opentype;

import info.joriki.io.FullySeekableDataInput;

import java.io.IOException;

public class SpecifiedSingleSubstitution implements SingleSubstitution {
  short [] glyphIDs;
  
  public SpecifiedSingleSubstitution(FullySeekableDataInput in) throws IOException {
    glyphIDs = new short [in.readUnsignedShort ()];
    for (int i = 0;i < glyphIDs.length;i++)
      glyphIDs [i] = in.readShort ();
  }

  public void writeTo(OffsetOutputStream out) {
    out.writeShort (glyphIDs.length);
    for (int i = 0;i < glyphIDs.length;i++)
      out.writeShort (glyphIDs [i]);
  }
}
