/*
 * Copyright 2003 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.jp2;

import java.io.IOException;

import info.joriki.io.BitSource;

import info.joriki.util.General;

public class TagTreeDecoder
{
  int w;
  int h;

  int nlevels;

  int [] [] values;
  boolean [] [] done;

  public TagTreeDecoder (int w,int h)
  {
    this.w = w;
    this.h = h;

    nlevels = General.bitLength (Math.max (w,h) - 1) + 1;
    values = new int [nlevels] [];
    done = new boolean [nlevels] [];

    for (int i = 0;i < nlevels;i++)
      {
	int nnodes = w * h;
	values [i] = new int [nnodes];
	done [i] = new boolean [nnodes];
	w = (w + 1) >> 1;
	h = (h + 1) >> 1;
      }
  }

  final int decode (BitSource source,int x,int y) throws IOException
  {
    // workaround for Sun bug 6559156:
    // incorrect comparison to Integer.MAX_VALUE in server version
    // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6559156
    return decode (source,x,y,Integer.MAX_VALUE - 1);
  }

  int decode (BitSource source,int x,int y,int thresh) throws IOException
  {
    int dad = 0;

    for (int level = nlevels - 1;level >= 0;level--)
      {
	int index = (y >> level) * (((w - 1) >> level) + 1) + (x >> level);
	int value = values [level] [index];
	if (!done [level] [index])
	  {
	    if (dad > value)
	      value = dad;
	    while (value <= thresh)
	      {
		if (source.readBits (1) == 0)
		  value++;
		else
		  {
		    done [level] [index] = true;
		    break;
		  }
	      }
	    values [level] [index] = value;
	  }
	if (value > thresh)
	  return value;
	dad = value;
      }
    return dad;
  }
}
