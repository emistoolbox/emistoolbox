/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.crypto;

public class MD5 extends ManglingDigest
{
  final static Mangler [] manglers = {
    conditionalMangler,
    new Mangler () {
        public int mangle (int x,int y,int z) {
          return (x & z) | (y & ~z);
        }
      },
    xorMangler,
    new Mangler () {
        public int mangle (int x,int y,int z) {
          return y ^ (x | ~z);
        }
      }
  };

  final static int [] offsets = {0,1,5,0};
  final static int [] periods = {1,5,3,7};
  final static int [] [] shifts = {
    {7,12,17,22},
    {5,9,14,20},
    {4,11,16,23},
    {6,10,15,21}
  };

  final static int [] table = new int [64];
  static {
    for (int i = 0;i < table.length;i++)
      table [i] = (int) (long) ((1L << 32) * Math.abs (Math.sin (i + 1)));
  }

  public MD5 ()
  {
    super (4,true);
  }

  protected void mangle ()
  {
    for (int i = 0,l = 0;i < 4;i++)
      {
        int period = periods [i];
        int [] shift = shifts [i];
        Mangler mangler = manglers [i];
        for (int j = 0,m = offsets [i];j < 16;j++,m += period)
	  state [(j + 3) & 3] = state [(j + 2) & 3] + rotateLeft
	    (state [(j + 3) & 3] +
	     mangler.mangle
	     (state [(j + 2) & 3],state [(j + 1) & 3],state [(j + 0) & 3]) +
	     x [m & 15] + table [l++],shift [j & 3]);
      }
  }
}
