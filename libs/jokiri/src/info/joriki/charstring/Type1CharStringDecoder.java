/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.charstring;

import info.joriki.io.ByteStream;

import info.joriki.util.Assertions;
import info.joriki.util.NotImplementedException;

public class Type1CharStringDecoder extends CharStringDecoder
{
  public Type1CharStringDecoder (CharStringFont font)
  {
    super (font,font.getSubroutines (),1);
  }

  int flexPoint;
  public double [] weights;

  double [] otherstack = new double [6];
  int notherstack;

  protected void initialize ()
  {
    flexPoint = 0;
    super.initialize ();
  }

  private void rmoveto (double dx,double dy)
  {
    if (flexPoint != 0) // flesh out point for flex
      {
        stack [nstack++] = dx;
        stack [nstack++] = dy;
      }
    else
      {
        /* gstype2.c in the Ghostscript source says that
           the subpath is closed implicitly here, whereas
           in Type 2 charstrings it's closed explicitly,
           and that this makes a difference in the PostScript
           charpath operator. I'm not entirely sure what
           this means, but I don't think it matters for us. */
   
        nextPath ();
        interpreter.rmoveto (dx,dy);
        nstack = 0;
      }
  }

  public void command (int r,ByteStream byteStream)
  {
    switch (r)
      {
      case 1 : // hstem
        if (interpreter instanceof HintedCharStringInterpreter)
          ((HintedCharStringInterpreter) interpreter).hstem
            (ySideBearing + stack [0],stack [1]);
        nstack = 0;
        break;
      case 3 : // vstem
        if (interpreter instanceof HintedCharStringInterpreter)
          ((HintedCharStringInterpreter) interpreter).vstem
            (xSideBearing + stack [0],stack [1]);
        nstack = 0;
        break;
      case 4 : // vmoveto
        rmoveto (0,stack [--nstack]);
        return; // skip stack clearing
      case 5 :  // rlineto
        interpreter.rlineto (stack [0],stack [1]);
        break;
      case 6 :  // hlineto
        interpreter.rlineto (stack [0],0);
        break;
      case 7 : // vlineto
        interpreter.rlineto (0,stack [0]);
        break;
      case 8 : // rrcurveto
        interpreter.rrcurveto
          (stack [0],stack [1],stack [2],stack [3],stack [4],stack [5]);
        break;
      case 9 : // closepath
        nextPath ();
        break;
      case 13 : // hsbw
        setSideBearing (stack [0],0);
        setAdvance (stack [1],0);
        break;
      case 14 : // endchar
        interpreter.closepath ();
        break;
      case 21 : // rmoveto
        if (flexPoint != 0) // args can stay as they are for flex
          return; // skip stack clearing
        nextPath ();
        interpreter.rmoveto (stack [0],stack [1]);
        break;
      case 22 : // hmoveto
        rmoveto (stack [--nstack],0);
        return; // skip stack clearing
      case 30 : // vhcurveto
        interpreter.rrcurveto
          (0,stack [0],stack [1],stack [2],stack [3],0);
        break;
      case 31 : // hvcurveto
        interpreter.rrcurveto
          (stack [0],0,stack [1],stack [2],0,stack [3]);
        break;
      default :
        command (r);
        return;
      }
    nstack = 0;
  }

  private void setSideBearing (double x,double y)
  {
    interpreter.rmoveto (x,y);
    xSideBearing = x;
    ySideBearing = y;
  }

  public void escape (int b)
  {
    switch (b)
      {
      case 1  : // vstem3
        if (interpreter instanceof HintedCharStringInterpreter)
          for (int i = 0;i < 6;i += 2)
            ((HintedCharStringInterpreter) interpreter).vstem
              (xSideBearing + stack [i],stack [i + 1]);
        nstack = 0;
        break;
      case 2  : // hstem3
        if (interpreter instanceof HintedCharStringInterpreter)
          for (int i = 0;i < 6;i += 2)
            ((HintedCharStringInterpreter) interpreter).hstem
              (ySideBearing + stack [i],stack [i + 1]);
        nstack = 0;
        break;
      case 6 : // seac
        seac (1,stack [0]);
        break;
      case 7 : // sbw
        setSideBearing (stack [0],stack [1]);
        setAdvance (stack [2],stack [3]);
        nstack = 0;
        break;
      case 16 : // callothersubr
        int index = (int) stack [nstack - 1];
        nstack -= stack [nstack - 2] + 2;
        switch (index)
          {
          case 0 :
            Assertions.expect (flexPoint,8);
            flexPoint = 0;
            interpreter.flex (stack);
            // two arbitrary coordinates for new current point that aren't used
            notherstack += 2;
            break;
          case 1 :
            Assertions.expect (flexPoint,0);
            flexPoint = 1;
            break;
          case 2 :
            if (flexPoint++ == 2)
              {
                stack [0] += stack [2];
                stack [1] += stack [3];
                nstack -= 2;
              }
            break;
          case 3 :
            otherstack [notherstack++] = stack [nstack];
            if (interpreter instanceof HintedCharStringInterpreter)
              ((HintedCharStringInterpreter) interpreter).changeHints ();
            break;
          case 18 :
            index++;
          case 17 :
          case 16 :
          case 15 :
          case 14 :
            int m = index - 13;     // number of parameters
            int n = weights.length; // number of weights
            index = nstack + m;
            // calculate m values on the stack, increasing nstack by m
            for (int i = 0;i < m;i++,nstack++)
              for (int j = 1;j < n;j++,index++)
                stack [nstack] += weights [j] * stack [index];
            // now move them to the PostScript stack, restoring nstack
            for (int i = 0;i < m;i++)
              otherstack [notherstack++] = stack [--nstack];
            break;
          default :
            throw new NotImplementedException ("othersubr " + index);
          }
        break;
      case 17 :
        stack [nstack++] = otherstack [--notherstack];
        break;
      case 33 : // setcurrentpoint
        nstack = 0;
        break;
      default :
        super.escape (b);
      }
  }
}
