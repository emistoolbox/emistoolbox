/*
 * Copyright 2003 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.jp2;

import java.io.DataInput;
import java.io.IOException;

import java.awt.Rectangle;

import java.util.List;
import java.util.ArrayList;

import info.joriki.io.BitSource;

import info.joriki.util.General;

class Block extends Rectangle implements JP2Speaker
{
  final static int [] passBits = {1,1,2,5,7};

  List segments = new ArrayList ();

  boolean active;

  int npasses;
  int lblock = 3;
  int segmentIndex;
  int zeroBitPlanes;

  Block (Rectangle patch)
  {
    super (patch);
  }

  void decodePacketHeader (BitSource source,int layer,Band band,int x,int y) throws IOException
  {
    if (active)
      {
	if (source.readBits (1) == 0)
	  return;
      }
    else
      {
	int firstLayer = band.inclusionDecoder.decode (source,x,y,layer);
	if (firstLayer > layer)
	  return;
	active = true;
	zeroBitPlanes = band.zeroPlanesDecoder.decode (source,x,y);
      }

    int beg = npasses++;

    for (int i = 0;;)
      {
	int nbit = passBits [i];
	int mask = (1 << nbit) - 1;
	int bits = source.readBits (nbit);
	if (bits != mask || ++i == passBits.length)
	  {
	    npasses += bits;
	    break;
	  }
	npasses += mask;
      }

    while (source.readBits (1) == 1)
      lblock++;

    // avoid the loop if we know there's only on segment
    int pass = band.codingStyle.normalTermination ? npasses - 1 : beg;

    while (pass < npasses)
      {
	boolean terminal = band.codingStyle.terminal (pass);
	if (++pass == npasses || terminal)
	  {
	    Segment segment = new Segment ();
	    segment.data = new byte [source.readBits (lblock + General.bitLength (pass - beg) - 1)];
	    segment.terminal = terminal;
	    segments.add (segment);
	    beg = pass;
	  }
      }
  }

  void readPacketData (DataInput in) throws IOException
  {
    while (segmentIndex < segments.size ())
      in.readFully (((Segment) segments.get (segmentIndex++)).data);
  }
}
