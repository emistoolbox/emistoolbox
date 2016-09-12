/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.crypto;

import info.joriki.util.NotImplementedException;

public class PostScriptCypher implements StreamCypher
{
  public final static int CHARSTR = 4330;
  public final static int EEXEC = 55665;
  private final static int c1 = 52845;
  private final static int c2 = 22719;

  final int r1;
  int r;

  public PostScriptCypher (int r1)
  {
    this.r1 = r1;
    reset ();
  }

  public void reset ()
  {
    r = r1;
  }

  public byte decrypt (int b)
  {
    b &= 0xff;
    byte res = (byte) (b ^ (r >> 8));
    r = ((b + r) * c1 + c2) & 0xffff;
    return res;
  }

  public byte encrypt (int b)
  {
    throw new NotImplementedException ("PostScript encryption");
  }
}
