/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.charstring;

import info.joriki.io.ByteStream;

import info.joriki.font.Widths;
import info.joriki.font.InvalidGlyphException;

import info.joriki.util.Options;
import info.joriki.util.Assertions;

public class Type2CharStringDecoder extends CharStringDecoder
{
  byte [] [] globalSubroutines;
  Widths widths;

  public Type2CharStringDecoder (CharStringFont font,
                                 byte [] [] subroutines,
                                 byte [] [] globalSubroutines,
                                 Widths widths)
  {
    super (font,subroutines,2);
    this.globalSubroutines = globalSubroutines;
    this.widths = widths;
  }

  int nhint;
  boolean moved;
  boolean advanced;

  protected void initialize ()
  {
    nhint = 0;
    moved = false;
    advanced = false;
    super.initialize ();
  }

  public void command (int r,ByteStream byteStream)
  {
    int i = 0;
    int remainder;
    
    if (info.joriki.util.Options.tracing)
      System.out.println ("interpreting " + r);

    switch (r)
      {
      case 0 : /* The percent glyph in font GoudyOldStyleBT-Italic in
                  files 1410600084_39.pdf, 1410600661_127.pdf and
                  1410600904_33.pdf contains a 0 command. The Type 2
                  spec marks this as reserved. Acrobat, freetype and
                  ghostscript all treat this as invalid; Acrobat shows
                  nothing. */
        throw new InvalidGlyphException ("invalid command " + r + " in charstring");
      case 1 : // hstem
      case 3 : // vstem
      case 18 : // hstemhm
      case 23 : // vstemhm
        nhint += nstack >> 1;
        nstack &= 1; // leave optional width on stack
        return; // skip stack clearing
      case 4 : // vmoveto
        interpreter.rmoveto (0,stack [getMoveIndex (1)]);
        moved = true;
        break;
      case 5 :  // rlineto
        beginSubpath ();
        for (;i < nstack;i += 2)
          interpreter.rlineto (stack [i],stack [i + 1]);
        break;
      case 6 :  // hlineto
        interpreter.rlineto (stack [i++],0);
        // fall through
      case 7 :  // vlineto
        beginSubpath ();
        while (i < nstack)
          {
            interpreter.rlineto (0,stack [i++]);
            if (i == nstack)
              break;
            interpreter.rlineto (stack [i++],0);
          }
        break;
      case 8 : // rrcurveto
        beginSubpath ();
        /* occurs in space glyph in fonts Mycalc and Amethyst in
           1888729066_4.pdf. Probably not meant as an rrcurveto,
           since no move precedes it. freetype treats it as invalid;
           ghostscript just uses any complete sets of six
           arguments and discards the rest. Can't tell what
           Adobe does since it's a space anyway. */
        if ((nstack % 6) != 0)
          throw new InvalidGlyphException ("wrong argument count in charstring");
        for (;i < nstack;i += 6)
          interpreter.rrcurveto (stack [i],stack [i + 1],stack [i + 2],
                                 stack [i + 3],stack [i + 4],stack [i + 5]);
        break;
      case 14 : // endchar
        boolean isSeac = nstack >= 4;
        int first = getMoveIndex (isSeac ? 4 : 0);
        if (isSeac)
          seac (first,0);
        else
          interpreter.closepath ();
        break;
      case 19 : // hintmask
      case 20 : // cntrmask
        nhint += nstack >> 1; // vstemhm may be omitted
        byteStream.skip ((nhint + 7) >> 3);
        nstack &= 1; // leave optional width on stack
        return; // skip stack clearing
      case 21 : // rmoveto
        if (nstack > 3)
          // occurs in 1410600211_90.pdf
          // The charstring has hstemhm directly followed by rmoveto
          // with 5 arguments. Argument 0 is the width, and arguments
          // 3 and 4 are intended for rmoveto. Apparently arguments
          // 1 and 2 are intended for an implicit vstemhm, which may
          // be omitted before a hintmask command, but the hintmask
          // command is missing. In 0060538902.pdf the hintmask command
          // actually follows after the rmoveto.
          // We pop the arguments off the stack, execute the missing
          // vstemhm command and then reinstate the arguments. For 
          // 0060538902.pdf it's important to execute the vstemhm
          // since it changes the number of mask bytes that the
          // hintmask operator takes. 
          {
            Options.warn ("excess arguments to rmoveto");
            int index = nstack -= 2;
            command (23,byteStream); // vstemhm
            argument (stack [index]);
            argument (stack [index + 1]);
          }
        int moveIndex = getMoveIndex (2);
        interpreter.rmoveto (stack [moveIndex],stack [moveIndex + 1]);
        moved = true;
        break;
      case 22 : // hmoveto
        interpreter.rmoveto (stack [getMoveIndex (1)],0);
        moved = true;
        break;
      case 24 : // rcurveline
        beginSubpath ();
        Assertions.expect ((nstack - 2) % 6,0);
        for (;i < nstack - 2;i += 6)
          interpreter.rrcurveto (stack [i],    stack [i + 1],
                                 stack [i + 2],stack [i + 3],
                                 stack [i + 4],stack [i + 5]);
        interpreter.rlineto (stack [nstack - 2],stack [nstack - 1]);
        break;
      case 25 : // rlinecurve
        beginSubpath ();
        Assertions.expect (nstack >= 6);
        Assertions.expect (nstack & 1,0);
        for (;i < nstack - 6;i += 2)
          interpreter.rlineto (stack [i],stack [i+1]);
        interpreter.rrcurveto (stack [nstack - 6],stack [nstack - 5],
                               stack [nstack - 4],stack [nstack - 3],
                               stack [nstack - 2],stack [nstack - 1]);
        break;
      case 26 : // vvcurveto
        beginSubpath ();
        remainder = nstack & 3;
        if (remainder == 1)
          {
            interpreter.rrcurveto (stack [0],stack [1],
                                   stack [2],stack [3],
                                   0,        stack [4]);
            i = 5;
          }
        else
          Assertions.expect (remainder,0);
        for (;i < nstack;i += 4)
          interpreter.rrcurveto (0,            stack [i],
                                 stack [i + 1],stack [i + 2],
                                 0,            stack [i + 3]);
        break;
      case 27 : // hhcurveto
        beginSubpath ();
        remainder = nstack & 3;
        if (remainder == 1)
          {
            interpreter.rrcurveto (stack [1],stack [0],
                                   stack [2],stack [3],
                                   stack [4],0);
            i = 5;
          }
        else
          Assertions.expect (remainder,0);
        for (;i < nstack;i += 4)
          interpreter.rrcurveto (stack [i],    0,
                                 stack [i + 1],stack [i + 2],
                                 stack [i + 3],0);
        break;
      case 29 : // callgsubr
        call (globalSubroutines);
        return; // skip stack clearing
      case 30 : // vhcurveto
      case 31 : // hvcurveto
        beginSubpath ();
        while (i + 1 < nstack)
          {
            if (!(i == 0 && r == 30)) // skip horizontal on vh
              {
                double oddArg = i == nstack - 5 ? stack [i + 4] : 0;
                interpreter.rrcurveto (stack [i],    0,
                                       stack [i + 1],stack [i + 2],
                                       oddArg,       stack [i + 3]);
                i += 4;
              }
            if (i + 1 < nstack)
              {
                double oddArg = i == nstack - 5 ? stack [i + 4] : 0;
                interpreter.rrcurveto (0,            stack [i],
                                       stack [i + 1],stack [i + 2],
                                       stack [i + 3],oddArg);
                i += 4;
              }
          }
        break;
      default :
        command (r);
        return;
      }

    nstack = 0;
  }

  public void escape (int b)
  {
    switch (b)
      {
        // both truetype and ghostscript seem to believe,
        // contrary to the specs, that the flex operators
        // don't clear the stack. I'm using assertions here
        // to make sure we notice if arguments are dumped
        // by the stack clearing.
      case 34 : // hflex
        Assertions.expect (nstack,7);
        stack [12] = 50;
        stack [11] = 0;
        stack [10] = stack [6];
        stack [9] = -stack [2];
        stack [8] = stack [5];
        stack [7] = 0;
        stack [6] = stack [4];
        stack [5] = 0;
        stack [4] = stack [3];
        stack [3] = stack [2];
        stack [2] = stack [1];
        stack [1] = 0;
        stack [0] = stack [0];
        interpreter.flex (stack);
        break;
      case 35 : // flex
        Assertions.expect (nstack,13);
        interpreter.flex (stack);
        break;
      case 36 : // hflex1
        Assertions.expect (nstack,9);
        stack [12] = 50;
        stack [11] = -(stack [1] + stack [3] + stack [7]);
        stack [10] = stack [8];
        stack [9] = stack [7];
        stack [8] = stack [6];
        stack [7] = 0;
        stack [6] = stack [5];
        stack [5] = 0;
        stack [4] = stack [4];
        stack [3] = stack [3];
        stack [2] = stack [2];
        stack [1] = stack [1];
        stack [0] = stack [0];
        interpreter.flex (stack);
        break;
      case 37 : // flex1
        Assertions.expect (nstack,11);
        double dx = 0;
        double dy = 0;
        for (int i = 0,k = 0;i < 5;i++)
          {
            dx += stack [k++];
            dy += stack [k++];
          }
        double lastArg = stack [--nstack];
        if (Math.abs (dx) > Math.abs (dy))
          {
            stack [nstack++] = lastArg;
            stack [nstack++] = -dy;
          }
        else
          {
            stack [nstack++] = -dx;
            stack [nstack++] = lastArg;
          }
        stack [nstack++] = 50;
        interpreter.flex (stack);
        break;
      case 128 : 
        /* occurs in space glyph in font Legacy in
           1888729066_220.pdf. freetype treats it as invalid;
           ghostscript lets unknown escape commands go through,
           but treats unknown one-byte commands as invalid;
           this might just be an oversight. Can't tell what
           Adobe does since it's a space anyway. */
        throw new InvalidGlyphException ("invalid escape command " + b + " in charstring");
      default :
        super.escape (b);
        return; // skip stack clearing
      }

    nstack = 0;
  }

  protected void beginSubpath ()
  {
    if (!moved)
      throw new InvalidGlyphException ("subpath does not start with move");
  }

  protected int getMoveIndex (int expect)
  {
    int index = nstack - expect;
    Assertions.limit (index,0,1);
    if (!advanced)
      {
        setAdvance (index == 1 ?
                    widths.nominalWidth + stack [0] :
                    widths.defaultWidth,
                    0);
        advanced = true;
      }
    nextPath ();
    return index;
  }

  protected void call (byte [] [] subs,int sub)
  {
    super.call (subs,sub + CharStrings.bias (subs.length));
  }
}
