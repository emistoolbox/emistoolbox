/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.jpeg;

import java.awt.Rectangle;

public class JPEGRequest extends Rectangle
{
  byte type = -1;
  byte min;
  byte max;

  JPEGRequest ()
  {
    this ((byte) -1);
  }

  JPEGRequest (byte type)
  {
    this (type,1,1);
  }

  JPEGRequest (byte min,byte max)
  {
    this (min,max,1,1);
  }

  JPEGRequest (byte type,int width,int height)
  {
    super (width,height);
    this.type = min = max = type;
  }

  JPEGRequest (byte min,byte max,int width,int height)
  {
    super (width,height);
    this.min = min;
    this.max = max;
  }

  JPEGRequest (byte type,int x,int y,int width,int height)
  {
    super (x,y,width,height);
    this.type = min = max = type;
  }

  JPEGRequest (byte min,byte max,int x,int y,int width,int height)
  {
    super (x,y,width,height);
    this.min = min;
    this.max = max;
  }
}
