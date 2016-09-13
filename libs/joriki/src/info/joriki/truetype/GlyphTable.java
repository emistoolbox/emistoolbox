/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.truetype;

import java.io.IOException;

import info.joriki.io.FullySeekableDataInput;
import info.joriki.io.Util;
import info.joriki.font.InvalidGlyphException;
import info.joriki.sfnt.HorizontalMetrics;

public class GlyphTable extends AbstractGlyphTable
{
  FullySeekableDataInput in;
  HorizontalMetrics horizontalMetrics;
  long tableOffset;

  public GlyphTable (FullySeekableDataInput in,
                     LocationTable locationTable,
                     HorizontalMetrics horizontalMetrics)
  {
    this.in = in;
    this.locationTable = locationTable;
    this.horizontalMetrics = horizontalMetrics;
    tableOffset = in.getOffset ();
  }

  public GlyphOutline getOutline (int index)
  {
	  if (index < 0 || index >= getOutlineCount ())
      throw new InvalidGlyphException ("invalid glyph index");
      
    if (locationTable.offsets [index + 1] == locationTable.offsets [index])
      return EmptyGlyphOutline.emptyGlyphOutline;

    GlyphOutline outline = (GlyphOutline) outlines.get (index);
    if (outline == null)
      try {
        in.seek (tableOffset + locationTable.offsets [index]);
        outline = new NonEmptyGlyphOutline
        (in,this,horizontalMetrics.leftSideBearing [index],horizontalMetrics.advanceWidth [index]);
        outlines.set (index,outline);
      } catch (IOException ioe) {
        throw new Error ("can't read glyph for index " + index);
      }
    return outline;
  }

  protected byte [] getOutlineData (int index) {
      return Util.toByteArray (getOutline (index));
  }
  
  public GlyphOutlineData getGlyphOutlineData (int index) throws IOException {
	  if (index < 0 || index >= getOutlineCount ())
		  throw new InvalidGlyphException ("invalid glyph index");
      in.seek (tableOffset + locationTable.offsets [index]);
      int length = locationTable.offsets [index + 1] - locationTable.offsets [index];
      // this isn't required; just checking it since ComplexGlyphData allows for alignment padding
      if ((length & 3) != 0)
    	  throw new Error ("unaligned glyph data");
      return length == 0 ?
    		  new EmptyGlyphOutlineData (index,horizontalMetrics.leftSideBearing [index],horizontalMetrics.advanceWidth [index]) :
    	      new NonEmptyGlyphOutlineData (in,length,index,horizontalMetrics.leftSideBearing [index],horizontalMetrics.advanceWidth [index]);
  }
  
  public byte [] getRawOutlineData (int index) throws IOException {
	  if (index < 0 || index >= getOutlineCount ())
		  throw new InvalidGlyphException ("invalid glyph index");
      byte [] data = new byte [locationTable.offsets [index + 1] - locationTable.offsets [index]];
      in.seek (tableOffset + locationTable.offsets [index]);
      in.readFully (data);
      return data;
  }
  
  public int getOutlineCount () {
	  return locationTable.offsets.length - 1;  // FP: added -1 24/12/2013 to account for extra offset
  }
}
