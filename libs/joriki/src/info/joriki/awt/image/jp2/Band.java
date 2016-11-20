/*
 * Copyright 2003 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.jp2;

import java.io.DataInput;
import java.io.IOException;

import java.awt.Rectangle;

import info.joriki.io.BitSource;

class Band extends Rectangle
{
  Block [] [] blocks;
  TagTreeDecoder inclusionDecoder;
  TagTreeDecoder zeroPlanesDecoder;
  CodingStyle codingStyle;
  int blockWidth;
  int blockHeight;
  int subx;
  int suby;

  Band (Rectangle patch,CodingStyle codingStyle)
  {
    super (patch);
    this.codingStyle = codingStyle;

    if (width * height == 0)
      return;

    PatchIterator blockIterator = new PatchIterator (this,codingStyle.blockSize,true);
    blockWidth = blockIterator.getWidth ();
    blockHeight = blockIterator.getHeight ();
    inclusionDecoder  = new TagTreeDecoder (blockWidth,blockHeight);
    zeroPlanesDecoder = new TagTreeDecoder (blockWidth,blockHeight);
    blocks = new Block [blockWidth] [blockHeight];
    for (int y = 0;y < blockHeight;y++)
      for (int x = 0;x < blockWidth;x++)
	blocks [x] [y] = new Block (blockIterator.nextPatch ());
  }

  void decodePacketHeader (BitSource source,int layer) throws IOException
  {
    for (int y = 0;y < blockHeight;y++)
      for (int x = 0;x < blockWidth;x++)
	blocks [x] [y].decodePacketHeader (source,layer,this,x,y);
  }

  void readPacketData (DataInput in) throws IOException
  {
    for (int y = 0;y < blockHeight;y++)
      for (int x = 0;x < blockWidth;x++)
	blocks [x] [y].readPacketData (in);
  }
}
