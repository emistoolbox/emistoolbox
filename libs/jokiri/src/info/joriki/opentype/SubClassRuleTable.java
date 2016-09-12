/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.opentype;

import java.io.DataInput;
import java.io.IOException;

public class SubClassRuleTable implements OffsetTable {
  int [] classes;
  SubstitutionLookup [] lookups;
  
  public SubClassRuleTable (DataInput in) throws IOException {
    classes = new int [in.readUnsignedShort () - 1];
    lookups = new SubstitutionLookup [in.readUnsignedShort ()];
    for (int i = 0;i < classes.length;i++)
      classes [i] = in.readUnsignedShort ();
    for (int i = 0;i < lookups.length;i++)
      lookups [i] = new SubstitutionLookup (in);
  }
  public void writeTo(OffsetOutputStream out) {
    out.writeShort (classes.length + 1);
    out.writeShort (lookups.length);
    for (int i = 0;i < classes.length;i++)
      out.writeShort (classes [i]);
    for (int i = 0;i < lookups.length;i++)
      lookups[i].writeTo (out);
  }
}
