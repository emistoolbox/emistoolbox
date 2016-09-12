/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.jpeg;

import info.joriki.awt.image.CartesianTransform;

class MCU
{
  MCULayout layout;
  byte type;

  short [] [] [] shortData;
  float [] [] [] floatData;
  byte [] byteData;
  int [] pixelData;

  MCU (MCULayout layout)
  {
    this.layout = layout;
  }

  final void deallocate (byte type)
  {
    layout.deallocate (type,this);
  }

  final void allocate (byte type)
  {
    layout.allocate (type,this);
  }

  final void transformTo (byte type)
  {
    layout.transformTo (type,type,this);
  }

  final void transformToMeet (JPEGRequest request)
  {
    layout.transformTo (request.min,request.max,this);
  }

  final void copy (MCU mcu)
  {
    layout.copy (mcu,this);
  }

  final void makeTransformedCopy (MCU mcu,CartesianTransform transform)
  {
    layout.makeTransformedCopy (mcu,this,transform);
  }

  final void clear ()
  {
    layout.clear (this);
  }
}
