/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.truetype;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;

import java.util.List;
import java.util.ArrayList;

import info.joriki.io.Util;

import info.joriki.util.Assertions;

import info.joriki.graphics.Transformation;

public class ComplexGlyphDescription extends GlyphDescription implements ComplexGlyphSpeaker
{
  GlyphTable glyphTable;
  List components = new ArrayList ();

  int metricIndex = -1;

  class GlyphComponent
  {
    int glyphIndex;
    double a00,a01;
    double a10,a11;
    int arg1,arg2;
    boolean match;
    Transformation scale;
  }

  NonEmptyGlyphOutline getMetricOutline ()
  {
    return metricIndex == -1 ? null : (NonEmptyGlyphOutline) glyphTable.getOutline (((GlyphComponent) components.get (metricIndex)).glyphIndex);
  }

  public ComplexGlyphDescription (DataInput in,GlyphTable glyphTable) throws IOException
  {
    this.glyphTable = glyphTable;

    boolean hasInstructions = false;
    short flags;
    
    do
      {
        flags = in.readShort ();
        // we ignore:
        // ROUND_TO_XY_GRID (which is self-explanatory) and
        // OVERLAP_COMPOUND (which means that the components overlap)

        hasInstructions |= (flags & HAS_INSTRUCTIONS) == HAS_INSTRUCTIONS;

        GlyphComponent c = new GlyphComponent ();
        c.glyphIndex = in.readUnsignedShort ();

        if ((flags & USE_MY_METRICS) == USE_MY_METRICS)
          metricIndex = components.size ();

        if ((flags & ARGS_ARE_WORDS) == ARGS_ARE_WORDS)
          {
            c.arg1 = in.readShort ();
            c.arg2 = in.readShort ();
          }
        else
          {
            c.arg1 = in.readByte ();
            c.arg2 = in.readByte ();
          }

        c.match = (flags & ARGS_ARE_XY_VALUES) != ARGS_ARE_XY_VALUES;

        if ((flags & HAS_TWO_BY_TWO) == HAS_TWO_BY_TWO)
          {
            c.a00 = readFractionalFrom (in);
            c.a01 = readFractionalFrom (in);
            c.a10 = readFractionalFrom (in);
            c.a11 = readFractionalFrom (in);
          }
        else
          {
            c.a10 = c.a01 = 0;

            if ((flags & HAS_X_AND_Y_SCALE) == HAS_X_AND_Y_SCALE)
              {
                c.a00 = readFractionalFrom (in);
                c.a11 = readFractionalFrom (in);
              }
            else if ((flags & HAS_SCALE) == HAS_SCALE)
              c.a00 = c.a11 = readFractionalFrom (in);
            else
              c.a00 = c.a11 = 1;
          }

        /* These flags are explained in the BUGS file of the
           freetype sources. I've never seen them set.
           m != 1 || n != 1 occurs in mssong.ttf and in
	       font BDOAIB+Wingdings3 in 159140083X.pdf, and
	       in both cases the results were only correct with
	       unscaled offsets. This would be the scaling:
   
	if (!c.match)
	  {
        double m = Math.max (Math.abs (c.a00),Math.abs (c.a01));
        double n = Math.max (Math.abs (c.a10),Math.abs (c.a11));
	    c.arg1 *= m;
	    c.arg2 *= n;
	  }
	*/

//        Assertions.expect (flags &
//                           (SCALED_COMPONENT_OFFSET |
//                            UNSCALED_COMPONENT_OFFSET),
//                           0);

        components.add (c);
      }
    while ((flags & MORE_COMPONENTS) == MORE_COMPONENTS);

    if (hasInstructions)
      instructions = Util.readBytes (in,in.readUnsignedShort ());
  }

  public void writeTo (DataOutput out) throws IOException
  {
    ByteArrayOutputStream baos = new ByteArrayOutputStream (12); // for arguments
    DataOutputStream dos = new DataOutputStream (baos);

    for (int i = 0;i < components.size ();i++)
      {
        GlyphComponent c = (GlyphComponent) components.get (i);

        short flags = 0;

        if (!c.match)
          flags |= ARGS_ARE_XY_VALUES;
        if (i != components.size () - 1)
          flags |= MORE_COMPONENTS;
        else if (instructions != null)
          flags |= HAS_INSTRUCTIONS;
        if (i == metricIndex)
          flags |= USE_MY_METRICS;

        baos.reset ();

        int x = c.arg1;
        int y = c.arg2;

        if (x == ((byte) x) && y == ((byte) y))
          {
            dos.write (x);
            dos.write (y);
          }
        else
          {
            flags |= ARGS_ARE_WORDS;
            dos.writeShort (x);
            dos.writeShort (y);
          }

        if (c.a01 != 0 || c.a10 != 0)
          {
            flags |= HAS_TWO_BY_TWO;
            writeFractionalTo (out,c.a00);
            writeFractionalTo (out,c.a01);
            writeFractionalTo (out,c.a10);
            writeFractionalTo (out,c.a11);
          }
        else if (c.a00 != c.a11)
          {
            flags |= HAS_X_AND_Y_SCALE;
            writeFractionalTo (out,c.a00);
            writeFractionalTo (out,c.a11);
          }
        else if (c.a00 != 1)
          {
            flags |= HAS_SCALE;
            writeFractionalTo (out,c.a00);
          }

        out.writeShort (flags);
        out.writeShort (c.glyphIndex);
        out.write (baos.toByteArray ());
      }

    if (instructions != null)
      {
        out.writeShort (instructions.length);
        out.write (instructions);
      }
  }
  
  final static double shift = 0x4000;

  private final static double readFractionalFrom (DataInput in) throws IOException
  {
    return in.readShort () / shift;
  }

  private final static void writeFractionalTo (DataOutput out,double x) throws IOException
  {
    int result = Math.round ((float) (x * shift));
    Assertions.expect ((short) result, result);
    out.writeShort (result);
  }

  public void interpret (ByteCodeInterpreter interpreter)
  {
    interpreter.plow ();

    for (int i = 0;i < components.size ();i++)
      {
        GlyphComponent component = (GlyphComponent) components.get (i);
        glyphTable.getOutline (component.glyphIndex).interpret (interpreter);
        double x,y;
        if (component.match)
          {
            double [] referencePoint = interpreter.getHarvest (component.arg1);
            double [] matchPoint = interpreter.getPoint (component.arg2);
            x = referencePoint [0] - matchPoint [0];
            y = referencePoint [1] - matchPoint [1];
          }
        else
          {
            x = component.arg1;
            y = component.arg2;
          }
        interpreter.harvest (new Transformation (new double [] {
          component.a00,
          component.a01,
          component.a10,
          component.a11,
          x,y}));
      }

    interpreter.sow ();
  }
}
