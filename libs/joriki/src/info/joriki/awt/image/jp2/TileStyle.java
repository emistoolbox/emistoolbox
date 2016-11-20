/*
 * Copyright 2003 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.jp2;

import java.io.DataInput;
import java.io.IOException;

import info.joriki.util.Assertions;

class TileStyle implements JP2Speaker
{
  final static int [] [] progressionOrders = {
    {PRECINCT,COMPONENT,RESOLUTION,LAYER},
    {PRECINCT,COMPONENT,LAYER,RESOLUTION},
    {LAYER,COMPONENT,PRECINCT,RESOLUTION},
    {LAYER,RESOLUTION,COMPONENT,PRECINCT},
    {LAYER,RESOLUTION,PRECINCT,COMPONENT}
  };

  boolean allowsSOP;
  boolean allowsEPH;

  int [] progressionOrder;
  int nlayers;
  int colorTransform;

  TileStyle (DataInput in,int flags) throws IOException
  {
    allowsSOP = (flags & SOP_MARKERS) != 0;
    allowsEPH = (flags & EPH_MARKERS) != 0;

    progressionOrder = progressionOrders [in.readUnsignedByte ()];
    nlayers = in.readUnsignedShort ();
    colorTransform = in.readUnsignedByte ();
    Assertions.limit (colorTransform,0,1);
  }
}
