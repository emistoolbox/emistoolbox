/*
 * Copyright 2003 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.jp2;

import java.io.DataInput;
import java.io.IOException;

import java.awt.Rectangle;

import info.joriki.io.BitSource;

import info.joriki.awt.image.YUVColorModel;

import info.joriki.util.Options;
import info.joriki.util.Assertions;
import info.joriki.util.NotTestedException;
import info.joriki.util.NotImplementedException;

class Tile extends Rectangle implements JP2Speaker
{
  int [] indices = new int [] {-1,0,0,0};
  TileComponent [] components;
  TileStyle tileStyle;
  int partIndex;
  int nparts;
  int packetNumber;

  Tile (Rectangle patch)
  {
    super (patch);
  }

  void initialize (BitDescriptor [] bitDescriptors,CodingStyle [] codingStyles,QuantizationStyle [] quantizationStyles,TileStyle tileStyle)
  {
    this.tileStyle = tileStyle;
    components = new TileComponent [bitDescriptors.length];
    Assertions.expect (quantizationStyles.length,components.length);
    Assertions.expect (codingStyles.length,components.length);
    for (int i = 0;i < components.length;i++)
      components [i] = new TileComponent (width,height,bitDescriptors [i],codingStyles [i],quantizationStyles [i]);
  }

  byte [] getBytes ()
  {
    Assertions.expect (components.length,1);
    return components [0].data;
  }

  int [] getInts ()
  {
    int [] pixels = new int [components [0].data.length];
    for (int k = 0;k < pixels.length;k++)
      {
	int pixel = 0;
	for (int i = 0;i < components.length;i++)
	  {
	    pixel <<= 8;
	    pixel |= components [i].data [k] & 0xff;
	  }
	pixels [k] = pixel;
      }
    return pixels;
  }

  void readPacket (BitSource bitSource,DataInput byteSource) throws IOException
  {
    Precinct precinct = null;
    for (int i = 0;i < 4;i++)
      {
	int which = tileStyle.progressionOrder [i];
	indices [which]++;
	if (indices [LAYER] < tileStyle.nlayers)
	  try {
	    precinct = 
	      components [indices [COMPONENT]].
	      resolutionLevels [indices [RESOLUTION]].
	      precincts [indices [PRECINCT]];
	    break;
	  } catch (ArrayIndexOutOfBoundsException aioobe) {}
	indices [which] = 0;
      }

    if (Options.tracing)
      {
	System.out.println ("resolution : " + indices [RESOLUTION]);
	System.out.println ("component  : " + indices [COMPONENT]);
	System.out.println ("precinct   : " + indices [PRECINCT]);
	System.out.println ("layer      : " + indices [LAYER]);
	System.out.println ();
      }

    if (tileStyle.allowsSOP)
      {
	if (byteSource.readUnsignedByte () != 0xff ||
	    byteSource.readUnsignedByte () != SOP)
	  throw new NotImplementedException ("missing SOP markers");
	Assertions.expect (byteSource.readUnsignedShort (),4);
	Assertions.expect (byteSource.readUnsignedShort (),packetNumber & 0xffff);
      }

    boolean notEmpty = bitSource.readBits (1) == 1;

    if (notEmpty)
      precinct.decodePacketHeader (bitSource,indices [LAYER]);

    if (tileStyle.allowsEPH)
      if (byteSource.readUnsignedByte () != 0xff ||
	  byteSource.readUnsignedByte () != EPH)
	throw new NotImplementedException ("missing EPH markers");
      
    if (notEmpty)
      precinct.readPacketData (byteSource);

    packetNumber++;
  }

  void transformColors ()
  {
    switch (tileStyle.colorTransform)
      {
      case 0 : break;
      case 1 :
	Assertions.expect (components.length >= 3);
	if
	  (components [0].floatBuf != null &&
	   components [1].floatBuf != null &&
	   components [2].floatBuf != null)
	  YUVColorModel.toRGB (components [0].floatBuf,components [1].floatBuf,components [2].floatBuf);
	else if
	  (components [0].intBuf != null &&
	   components [1].intBuf != null &&
	   components [2].intBuf != null)
	  inverseComponentTransform (components [0].intBuf,components [1].intBuf,components [2].intBuf);
	else
	  throw new NotImplementedException ("mixed component data");
	break;
      default :
	throw new NotImplementedException ("color transform " + tileStyle.colorTransform);
      }
  }

  void inverseComponentTransform (int [] rs,int [] gs,int [] bs)
  {
    Assertions.expect (gs.length,rs.length);
    Assertions.expect (bs.length,rs.length);
    for (int i = 0;i < rs.length;i++)
      {
	int r = rs [i];
	int g = gs [i];
	int b = bs [i];
	r -= (b + g) >> 2;
	rs [i] = r + b;
	gs [i] = r;
	bs [i] = r + g;
      }
  }

  void componentTransform (int [] rs,int [] gs,int [] bs)
  {
    Assertions.expect (gs.length,rs.length);
    Assertions.expect (bs.length,rs.length);
    for (int i = 0;i < rs.length;i++)
      {
	int r = rs [i];
	int g = gs [i];
	int b = bs [i];
	rs [i] = (r + g + g + b) >> 2;
	gs [i] = b - g;
	bs [i] = r - g;
      }
    throw new NotTestedException ();
  }

  void perform (int action)
  {
    for (int i = 0;i < components.length;i++)
      components [i].perform (action);
  }
}
