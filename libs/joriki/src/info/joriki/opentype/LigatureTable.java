/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.opentype;

import java.io.DataInput;
import java.io.IOException;

public class LigatureTable implements OffsetTable {
  int ligatureGlyph;
  int [] components;
  
  public LigatureTable(int ligatureGlyph,int [] components) {
    this.ligatureGlyph = ligatureGlyph;
    this.components = components;
  }

  public LigatureTable (DataInput in) throws IOException {
    ligatureGlyph = in.readUnsignedShort ();
    components = new int [in.readUnsignedShort () - 1];
    for (int i = 0;i < components.length;i++)
      components [i] = in.readUnsignedShort ();
  }
  
  public void writeTo (OffsetOutputStream out) {
    out.writeShort (ligatureGlyph);
    out.writeShort (components.length + 1);
    for (int i = 0;i < components.length;i++)
      out.writeShort (components [i]);
  }
}
