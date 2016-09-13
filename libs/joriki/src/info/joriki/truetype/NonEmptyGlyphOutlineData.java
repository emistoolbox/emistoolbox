/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.truetype;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

import info.joriki.font.InvalidGlyphException;

public class NonEmptyGlyphOutlineData extends GlyphOutlineData {
	int ncontour;
	byte [] box = new byte [8];
	GlyphData glyphData;
	
  public NonEmptyGlyphOutlineData (DataInput in,int nbytes,int index,short leftSideBearing,int advanceWidth) throws IOException
  {
	super (index,leftSideBearing,advanceWidth);

    ncontour = in.readShort ();
    
    in.readFully (box);

    if (ncontour < -1)
      throw new InvalidGlyphException ("invalid contour count " + ncontour);

    nbytes -= 10;
    
    switch (ncontour)
      {
      case -1 : glyphData = new ComplexGlyphData (in,nbytes);
        break;
      case 0  : glyphData = null;
        break;
      default : glyphData = new SimpleGlyphData (in,nbytes);
      }
  }

  public void writeTo (DataOutput out) throws IOException
  {
    out.writeShort (ncontour);
    out.write (box);
    if (glyphData != null)
      glyphData.writeTo (out);
  }
  
  public void map (GlyphOutlineData [] map) {
	  if (glyphData instanceof ComplexGlyphData)
		  ((ComplexGlyphData) glyphData).map (map);
  }

  public boolean equals (Object o) {
	  if (!(o instanceof NonEmptyGlyphOutlineData))
		  return false;
	  NonEmptyGlyphOutlineData glyphOutlineData = (NonEmptyGlyphOutlineData) o;
	  return super.equals (o) && glyphOutlineData.ncontour == ncontour && Arrays.equals (glyphOutlineData.box,box) && glyphOutlineData.glyphData.equals (glyphData);
  }
}
