/*
 * Copyright 2003 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.jp2;

import java.awt.Dimension;
import java.awt.Rectangle;

public class ResolutionLevel extends Rectangle
{
  Precinct [] precincts;
  ResolutionLevel nextLevel;

  ResolutionLevel (Dimension size,CodingStyle codingStyle,int level)
  {
    super (size);

    Rectangle [] [] subbands;
    if (level == 0)
      subbands = new Rectangle [] [] {{new Rectangle (this)}};
    else
      {
	subbands = new Rectangle [2] [2];
	for (int subx = 0,x = 0;subx < 2;subx++)
	  {
	    int subWidth = (width + 1 - subx) >> 1;
	    for (int suby = 0,y = 0;suby < 2;suby++)
	      {
		int subHeight = (height + 1 - suby) >> 1;
		subbands [subx] [suby] = new Rectangle (x,y,subWidth,subHeight);
		y += subHeight;
	      }
	    x += subWidth;
	  }
	nextLevel = new ResolutionLevel (subbands [0] [0].getSize (),codingStyle,level - 1);
      }

    PatchIterator precinctIterator = new PatchIterator (this,codingStyle.precinctSizes [level],false);
    precincts = new Precinct [precinctIterator.getCount ()];
    for (int i = 0;i < precincts.length;i++)
      precincts [i] = new Precinct (precinctIterator.nextPatch (),subbands,codingStyle);
  }
}
