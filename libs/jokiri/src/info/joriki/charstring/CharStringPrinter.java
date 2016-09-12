/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.charstring;

import java.io.PrintStream;

import info.joriki.io.ByteStream;

public class CharStringPrinter implements CharStringSpeaker
{
  PrintStream out;

  public CharStringPrinter (PrintStream out)
  {
    this.out = out;
  }

  public void command (int r,ByteStream byteStream)
  {
    out.println ("command " + r);
  }

  public void escape (int r)
  {
    out.println ("escape " + r);
  }

  public void argument (double a)
  {
    out.println ("argument : " + a);
  }
}
