/*
 * Copyright 2003 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.jp2;

import java.io.DataInput;
import java.io.IOException;

import java.awt.Rectangle;

import info.joriki.io.BitSource;

class Precinct extends Rectangle
{
  Band [] bands;

  Precinct (Rectangle patch,Rectangle [] [] subbands,CodingStyle codingStyle)
  {
    super (patch);
    int n = subbands.length;
    int x = patch.x / n;
    int y = patch.y / n;
    int width = patch.width / n;
    int height = patch.height / n;
    bands = new Band [n == 1 ? 1 : 3];
    for (int suby = 0,sub = 0;suby < n;suby++)
      for (int subx = 0;subx < n;subx++)
	if ((subx == 0 && suby == 0) == (n == 1))
	  {
	    Rectangle subband = subbands [subx] [suby];
	    bands [sub++] = new Band
	      (subband.intersection
	       (new Rectangle (subband.x + x,subband.y + y,width,height)),
	       codingStyle);
	  }
  }

  void decodePacketHeader (BitSource source,int layer) throws IOException
  {
    for (int i = 0;i < bands.length;i++)
      bands [i].decodePacketHeader (source,layer);
  }

  void readPacketData (DataInput in) throws IOException
  {
    for (int i = 0;i < bands.length;i++)
      bands [i].readPacketData (in);
  }
}
