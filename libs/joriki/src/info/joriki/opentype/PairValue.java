/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.opentype;

import info.joriki.util.NotImplementedException;

import java.io.DataInput;
import java.io.IOException;

public class PairValue implements OffsetTable {
  PairFormat format;
  int secondGlyph;
  int value1;
  int value2;
  
  public PairValue(DataInput in,PairFormat format) throws IOException {
    this.format = format;
    secondGlyph = in.readUnsignedShort ();
    value1 = readValue (in,format.valueFormat1);
    value2 = readValue (in,format.valueFormat1);
  }

  private int readValue (DataInput in,int format) throws IOException {
    switch (format) {
    case 0 : return 0;
    case 4 : return in.readUnsignedShort (); // couldn't find this documented
    default: throw new NotImplementedException ("value format " + format);
    }
  }
  
  public void writeTo(OffsetOutputStream out) {
    out.writeShort (secondGlyph);
    writeValue (out,format.valueFormat1,value1);
    writeValue (out,format.valueFormat2,value2);
  }
  
  private void writeValue (OffsetOutputStream out,int format,int value) {
    switch (format) {
    	case 0 : return;
    	case 4 : out.writeShort (value); break; // couldn't find this documented
    	default: throw new NotImplementedException ("value format " + format);
    }
  }
}
