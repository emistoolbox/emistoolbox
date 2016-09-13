/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.charstring;

import info.joriki.font.GlyphProvider;
import info.joriki.font.GlyphInterpreter;
import info.joriki.font.FatalInvalidGlyphException;

import info.joriki.util.NotImplementedException;

import info.joriki.adobe.Encoding;

abstract public class CharStringDecoder
  extends CharStringReader
  implements GlyphProvider, CharStringSpeaker
{
  protected CharStringFont font;
  protected CharStringInterpreter interpreter;

  protected byte [] [] subroutines;
  protected double [] stack = new double [48];
  protected boolean inComposite;
  protected int nstack;

  private byte [] charString;

  // only set non-zero by type 1
  double xSideBearing,ySideBearing;

  public CharStringDecoder
    (CharStringFont font,byte [] [] subroutines,int type)
  {
    super (type);
    this.font = font;
    this.subroutines = subroutines;
  }

  public void decode (byte [] charString,CharStringInterpreter interpreter)
  {
    setCharString (charString);
    interpret (interpreter);
  }

  public void setCharString (byte [] charString)
  {
    if (info.joriki.util.Options.tracing)
      read (charString,new CharStringPrinter (System.out));
    this.charString = charString;
  }

  protected void initialize ()
  {
    nstack = 0;
  }

  public void interpret (GlyphInterpreter interpreter)
  {
    interpret ((CharStringInterpreter) interpreter);
  }
  
  public void interpret (CharStringInterpreter interpreter)
  {
    this.interpreter = interpreter;
    inComposite = false;
    initialize ();
    interpreter.newpath ();
    read (charString,this);
    interpreter.finish ();
  }

  protected void nextPath ()
  {
    interpreter.closepath ();
    interpreter.newpath ();
  }

  public void command (int r)
  {
    switch (r)
      {
      case 10 : // callsubr
        call (subroutines);
        break;
      case 11 : // return
        break;
      case 15 : // undocumented
        // The GhostScript source code calls this
        // "an obsolete and undocumented command
        // used in some very old Adobe fonts" and
        // treats it as a no-op. So do we.
        nstack = 0;
        break;
      default :
        unimplement (r);
      }
  }
  
  public void argument (double r)
  {
    stack [nstack++] = r;
  }

  public void escape (int b)
  {
    switch (b)
      {
      case 0  : // dotsection, deprecated
        nstack = 0;
        break;
      case 12 : // div
        double den = stack [--nstack];
        double num = stack [--nstack];
        stack [nstack++] = num / den;
        break;
      default :
        throw new NotImplementedException
          ("charstring escape command " + (b & 0xff));
      }
  }

  protected void unimplement (int r)
  {
    throw new NotImplementedException ("charstring command " + r);
  }

  protected void call (byte [] [] subs)
  {
    call (subs,(int) stack [--nstack]);
  }

  protected void call (byte [] [] subs,int sub)
  {
    byte [] subroutine;
    try {
      subroutine = subs [sub];
    } catch (ArrayIndexOutOfBoundsException aioobe) {
      throw new FatalInvalidGlyphException ("invalid subroutine index " + sub);
    }
    read (subroutine,this);
  }

  private byte [] getStandardEncodedCharString (int code)
  {
    return font.getCharString (Encoding.standardEncoding.glyphs [code]);
  }

  protected void seac (int first,double asb)
  {
    byte [] bchar = getStandardEncodedCharString ((int) stack [first + 2]);
    byte [] achar = getStandardEncodedCharString ((int) stack [first + 3]);
    double adx = stack [first + 0];
    double ady = stack [first + 1];
    // I'm not sure what to make of this, but it works and seems
    // to be what ghostscript does. There's an erratum at the bottom
    // of p. 33 of 5015.Type1_Supp.pdf that's probably relevant to this. 
    adx += xSideBearing - asb;
    nstack = 0;
    inComposite = true;
    interpreter.moveto (0,0);
    if (bchar != null)
      read (bchar,this);
    if (achar != null)
      {
        initialize ();
        interpreter.newpath ();
        interpreter.moveto (adx,ady);
        read (achar,this);
      }
  }

  protected void setAdvance (double x,double y)
  {
    if (!inComposite)
      interpreter.setAdvance (x,y);
  }
}
