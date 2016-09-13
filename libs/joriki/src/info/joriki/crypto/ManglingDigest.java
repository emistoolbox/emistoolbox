/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.crypto;

public abstract class ManglingDigest extends AbstractDigest
{
  static interface Mangler
  {
    int mangle (int x,int y,int z);
  }

  final static Mangler xorMangler = new Mangler () {
      public int mangle (int x,int y,int z) {
	return x ^ y ^ z;
      }
    };
  final static Mangler conditionalMangler = new Mangler () {
      public int mangle (int x,int y,int z) {
	return (x & y) | ((~x) & z);
      }
    };
  final static Mangler pairMangler = new Mangler () {
      public int mangle (int x,int y,int z) {
	return (x & y) | (x & z) | (y & z);
      }
    };

  protected int [] x;
  protected int [] state;
  private int [] tmp;

  private int index;
  private int bytes;
  private long count;

  private boolean lsbFirst;

  protected ManglingDigest (int length,boolean lsbFirst)
  {
    this (length,lsbFirst,16);
  }
  
  protected ManglingDigest (int length,boolean lsbFirst,int xLength) {
    this.lsbFirst = lsbFirst;
    state = new int [length];
    tmp = new int [length];
    x = new int [xLength];
    reset ();
  }

  public void reset ()
  {
    int n = state.length;
    state [--n] = 0x67452301;
    state [--n] = 0xefcdab89;
    state [--n] = 0x98badcfe;
    state [--n] = 0x10325476;
    count = index = bytes = 0;
  }

  final int order (int n,int max,int shift)
  {
    return (lsbFirst ? n : max - n) << shift;
  }

  public void digest (int b)
  {
    int shift = order (bytes,3,3);
    x [index] &= ~(0xff << shift);
    x [index] |= (b & 0xff) << shift;
    count++;

    if (++bytes == 4)
      {
        bytes = 0;
        if (++index == 16)
          {
            process ();
            index = 0;
          }
      }
  }

  public byte [] getDigest ()
  {
    long finalCount = count << 3;
    digest (0x80);
    while (!(index == 14 && bytes == 0))
      digest (0);
    for (int i = 0;i < 2;i++)
      x [index++] = (int) (finalCount >> (order (i,1,5)));
    process ();

    byte [] result = new byte [state.length << 2];
    for (int i = state.length - 1,k = 0;i >= 0;i--)
      for (int j = 0;j < 4;j++,k++)
        result [k] = (byte) (state [i] >> order (j,3,3));
    return result;
  }

  protected static int rotateLeft (int value,int shift)
  {
    return (value << shift) + (value >>> (32 - shift));
  }

  private void process ()
  {
    for (int i = 0;i < state.length;i++)
      tmp [i] = state [i];

    mangle ();

    for (int i = 0;i < state.length;i++)
      state [i] += tmp [i];
  }

  abstract protected void mangle ();
}
