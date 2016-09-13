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

import info.joriki.io.Util;

import info.joriki.util.Options;

public class SimpleGlyphDescription extends GlyphDescription implements Flags
{
  int [] index; // index [i] : index of end point of contour i in points
  GlyphPoint [] points;

  public SimpleGlyphDescription (DataInput in,int ncontour) throws IOException
  {
    index = new int [ncontour];

    for (int i = 0;i < ncontour;i++)
      index [i] = in.readUnsignedShort ();

    instructions = Util.readBytes (in,in.readUnsignedShort ());

    points = new GlyphPoint [ncontour == 0 ? 0 : index [index.length - 1] + 1];
    for (int i = 0;i < points.length;i++)
      points [i] = new GlyphPoint ();
    for (int i = 0;i < points.length;i++)
      {
        GlyphPoint p = points [i];
        p.flags = in.readByte ();
        if ((p.flags & 0xc0) != 0)
          Options.warn ("reserved bit set in TrueType flags");
        if (p.isSet (REPEAT))
          for (int n = in.readUnsignedByte ();n > 0;n--)
            points [++i].flags = p.flags;
      }

    for (int coor = 0;coor < 2;coor++)
      for (int i = 0;i < points.length;i++)
        {
          int flags = points [i].flags >> coor;
          boolean flag1 = (flags & FLAG1) != 0;
          boolean flag2 = (flags & FLAG2) != 0;
          points [i].x [coor] =
            flag2 ? (short) ((flag1 ? +1 : -1) * in.readUnsignedByte ()) :
            (flag1 ? 0 : in.readShort ());
        }
  }

  public void writeTo (DataOutput out) throws IOException
  {
    for (int i = 0;i < index.length;i++)
      out.writeShort (index [i]);

    out.writeShort (instructions.length);
    out.write (instructions);

    for (int i = 0;i < points.length;i++)
      points [i].flags &= ONCURVE;

    ByteArrayOutputStream baos = new ByteArrayOutputStream ();
    DataOutputStream dos = new DataOutputStream (baos);
    byte [] [] coors = new byte [2] [];

    for (int coor = 0;coor < 2;coor++)
      {
        baos.reset ();
        for (int i = 0;i < points.length;i++)
          {
            GlyphPoint p = points [i];
            int x = p.x [coor];
            byte f = 0;
            if (x == 0)
              f = FLAG1;
            else if (-255 <= x && x <= 255)
              {
                f = FLAG2;
                if (x > 0)
                  f |= FLAG1;
                else
                  x = -x;
                dos.write (x);
              }
            else
              dos.writeShort (x);
            p.flags |= f << coor;
          }
        coors [coor] = baos.toByteArray ();
      }

    for (int i = 0;i < points.length;)
      {
        GlyphPoint p = points [i];
        int n = 0;
        while (++i < points.length && points [i].flags == p.flags)
          n++;
        if (n > 1)
          p.flags |= REPEAT;
        out.write (p.flags);
        if (n != 0)
          out.write (n == 1 ? p.flags : n);
      }
    
    for (int coor = 0;coor < 2;coor++)
      out.write (coors [coor]);
  }

  public void interpret (ByteCodeInterpreter interpreter)
  {
    interpreter.addIndex (index);

    double x = 0;
    double y = 0;
    for (int i = 0;i < points.length;i++)
      {
        GlyphPoint p = points [i];
        interpreter.addPoint
          (new double [] {x += p.x [0],y += p.x [1]},p.isSet (ONCURVE));
      }
  }
}
