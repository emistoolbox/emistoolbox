/*
 * Copyright 2003 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.jp2;

import java.io.IOException;

public class ArithmeticDecoder extends BitReader implements ArithmeticCoder
{
  int a;

  void initialize () throws IOException
  {
    super.initialize ();
    c <<= 8;
    nextByte ();
    c <<= 7;
    ct -= 7;
    a = mask;
  }

  public boolean readBit (ArithmeticCodingContext context) throws IOException
  {
    int q = context.state.q;
    int complement = a - q;
    boolean lower = c >>> 16 < q;
    a = lower ? q : complement;
    if (!lower)
      {
	c -= q << 16;
	if ((complement & mask) != 0)
	  return context.mps;
      }

    do
      nextBit ();
    while (((a <<= 1) & mask) == 0);

    return context.step (complement < q != lower);
  }
}
