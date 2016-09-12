/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.charstring;

import java.io.IOException;

import java.util.Set;
import java.util.List;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.Iterator;

import info.joriki.io.BitOutputStream;

import info.joriki.font.Widths;
import info.joriki.font.GlyphInterpreter;

import info.joriki.util.Assertions;
import info.joriki.util.ConsistentlyComparable;

public class Type2CharStringEncoder
  extends CharStringWriter
  implements GlyphInterpreter, HintedCharStringInterpreter
{
  List byteArrays = new ArrayList ();
  List [] hintList = new List [2];
  List [] hintLists = new List [2];
  Set [] hintSet = new Set [2];

  Widths widths;

  public Type2CharStringEncoder (Widths widths)
  {
    super (2);

    this.widths = widths;

    for (int i = 0;i < 2;i++)
      {
        hintList [i] = new ArrayList ();
        hintLists [i] = new ArrayList ();
        hintSet [i] = new TreeSet ();
      }
  }

  public void argument (double r)
  {
    super.argument (r);
    nargs++;
  }

  boolean first;
  int type;

  double fx = 0;
  double fy = 0;
  int ix = 0;
  int iy = 0;

  int nargs = 0;

  void escapeCommand (int command)
  {
    write (12);
    command (command);
  }

  void command (int command)
  {
    write (command);
    nargs = type = 0;
  }

  void flushCommand ()
  {
    if (type == 24)
      command (8); // rrcurveto
    else if (type == 25)
      command (5); // rlineto
  }

  void flushCommand (int flushed)
  {
    if (type == flushed)
      command (type);
    else
      type = 49 - flushed;
  }

  void point (double x,double y)
  {
    int nx = Math.round ((float) x);
    int ny = Math.round ((float) y);
    argument (nx - ix);
    argument (ny - iy);
    fx = x;
    fy = y;
    ix = nx;
    iy = ny;
  }

  void spaceFor (int n)
  {
    if (nargs + n > 48)
      flushCommand ();
  }

  public void rmoveto (double dx,double dy)
  {
    spaceFor (2);
    rpoint (dx,dy);
    command (21);
  }

  public void moveto (double x,double y)
  {
    rmoveto (x - fx,y - fy);
  }

  public void lineto (double x,double y)
  {
    spaceFor (2);
    point (x,y);
    flushCommand (24);
  }

  private void rpoint (double dx,double dy)
  {
    point (fx + dx,fy + dy);
  }
  
  public void rlineto (double dx,double dy)
  {
    spaceFor (2);
    rpoint (dx,dy);
    flushCommand (24);
  }

  public void rrcurveto (double dx1,double dy1,
                         double dx2,double dy2,
                         double dx3,double dy3)
  {
    spaceFor (6);
    rpoint (dx1,dy1);
    rpoint (dx2,dy2);
    rpoint (dx3,dy3);
    flushCommand (25);
  }

  public void flex (double [] args)
  {
    flushCommand ();
    for (int i = 0;i < 12;i += 2)
      rpoint (args [i + 0],args [i + 1]);
    argument (args [12]);
    escapeCommand (35);
  }

  double width;

  public void setAdvance (double wx,double wy)
  {
    width = wx;
    Assertions.expect (wy,0);
  }

  public void rrcurveto (double dx1,double dy1,double dx2,double dy2)
  {
    double cx1 = dx1 / 3;
    double cy1 = dy1 / 3;
    double cx2 = dx2 / 3;
    double cy2 = dy2 / 3;
    rrcurveto (dx1 - cx1,dy1 - cy1,
               cx1 + cx2,cy1 + cy2,
               dx2 - cx2,dy2 - cy2);
  }

  public void newpath ()
  {
    first = true;
  }

  public void closepath ()
  {
    flushCommand ();
  }

  public void finish ()
  {
    flushCommand ();
    command (14);
  }

  static class Hint extends ConsistentlyComparable
  {
    int x;
    int dx;

    Hint (double x,double dx)
    {
      this.x = Math.round ((float) x);
      this.dx = Math.round ((float) dx);
    }

    public int compareTo (Object o)
    {
      Hint h = (Hint) o;
      return x == h.x ? dx - h.dx : x - h.x;
    }
  }

  public void hstem (double y,double dy)
  {
    hintList [0].add (new Hint (y,dy));
  }

  public void vstem (double x,double dx)
  {
    hintList [1].add (new Hint (x,dx));
  }

  public void changeHints ()
  {
    flushCommand ();
    for (int i = 0;i < 2;i++)
      {
        hintLists [i].add (hintList [i]);
        hintSet [i].addAll (hintList [i]);
        hintList [i] = new ArrayList ();
      }
    byteArrays.add (super.toByteArray ());
    reset ();
  }

  public byte [] toByteArray ()
  {
    changeHints ();

    if (width != widths.defaultWidth)
      argument (width - widths.nominalWidth);

    boolean multipleHints = byteArrays.size () != 1;
    for (int i = 0;i < 2;i++)
      {
        double x = 0;
        Assertions.limit (hintSet [i].size (),0,47);
        Iterator iterator = hintSet [i].iterator ();
        while (iterator.hasNext ())
          {
            Hint hint = (Hint) iterator.next ();
            argument (hint.x - x);
            argument (hint.dx);
            x = hint.x + hint.dx;
          }
        command (multipleHints ?
                 (i == 0 ? 18 : 23) :
                 (i == 0 ? 1 : 3));
      }
    try {
      if (multipleHints)
        {
          BitOutputStream bos = new BitOutputStream (this,false);
          for (int j = 0;j < byteArrays.size ();j++)
            {
              command (19); // hintmask
              for (int i = 0;i < 2;i++)
                {
                  List hintList = (List) hintLists [i].get (j);
                  Iterator iterator = hintSet [i].iterator ();
                  while (iterator.hasNext ())
                    bos.write (hintList.contains (iterator.next ()));
                }
              bos.flush ();
              write ((byte []) byteArrays.get (j));
            }
        }
      else
        write ((byte []) byteArrays.get (0));
    } catch (IOException ioe) { throw new InternalError (); }

    return super.toByteArray ();
  }
}
