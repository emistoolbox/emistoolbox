/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.truetype;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import info.joriki.font.InvalidGlyphException;

public class NonEmptyGlyphOutline extends GlyphOutline
{
  short xmin;
  short ymin;
  short xmax;
  short ymax;

  short leftSideBearing;
  int advanceWidth;

  public NonEmptyGlyphOutline
    (DataInput in,GlyphTable glyphTable,short leftSideBearing,int advanceWidth)
    throws IOException
  {
    this.leftSideBearing = leftSideBearing;
    this.advanceWidth = advanceWidth;

    short ncontour = in.readShort ();

    xmin = in.readShort ();
    ymin = in.readShort ();
    xmax = in.readShort ();
    ymax = in.readShort ();

    if (ncontour < -1)
      throw new InvalidGlyphException ("invalid contour count " + ncontour);

    switch (ncontour)
      {
      case -1 : glyphDescription = new ComplexGlyphDescription (in,glyphTable);
        break;
      case 0  : glyphDescription = null;
        break;
      default : glyphDescription = new SimpleGlyphDescription (in,ncontour);
      }
  }

  public void writeTo (DataOutput out) throws IOException
  {
    out.writeShort (glyphDescription == null ? 0 :
                    glyphDescription instanceof SimpleGlyphDescription ?
                    ((SimpleGlyphDescription) glyphDescription).index.length :
                    -1);
    out.writeShort (xmin);
    out.writeShort (ymin);
    out.writeShort (xmax);
    out.writeShort (ymax);
    if (glyphDescription != null)
      glyphDescription.writeTo (out);
  }

  public void interpret (ByteCodeInterpreter interpreter)
  {
    if (glyphDescription == null)
      return;
    glyphDescription.interpret (interpreter);

    interpreter.addPoint (new double [] {xmin - leftSideBearing,0},false);
    interpreter.addPoint (new double [] {xmin - leftSideBearing + advanceWidth,0},false);
    interpreter.interpret (glyphDescription.instructions);

    int right = Math.round ((float) interpreter.popPhantomPoint () [0]);
    int left  = Math.round ((float) interpreter.popPhantomPoint () [0]);

    advanceWidth = right - left;
    leftSideBearing = (short) (xmin - left);

    NonEmptyGlyphOutline metricOutline = 
      glyphDescription instanceof ComplexGlyphDescription ?
      ((ComplexGlyphDescription) glyphDescription).getMetricOutline () :
      this;

    if (metricOutline == null)
      metricOutline = this;

    // in a composite, the last of these assignments, for the composite
    // itself, overwrites all the others; USE_MY_METRICS is treated above.
    interpreter.xoff = metricOutline.xmin - metricOutline.leftSideBearing;
  }
}
