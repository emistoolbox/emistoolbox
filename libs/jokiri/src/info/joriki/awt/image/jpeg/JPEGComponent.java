/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.jpeg;

import info.joriki.awt.image.CartesianTransform;

import info.joriki.util.Assertions;
import info.joriki.util.CloneableObject;

class JPEGComponent extends CloneableObject implements JPEGSpeaker
{
  int id;
  int hSamp;
  int vSamp;
  int quant;
  int size;

  short lastReadDC = 0;
  short lastWriteDC = 0;

  boolean [] done = new boolean [DCTsize];

  public JPEGComponent (int id,int hSamp,int vSamp,int quant)
  {
    Assertions.limit (hSamp,1,4);
    Assertions.unexpect (hSamp,3);
    Assertions.limit (vSamp,1,4);
    Assertions.unexpect (vSamp,3);
    Assertions.limit (quant,0,3);

    this.id = id;
    this.hSamp = hSamp;
    this.vSamp = vSamp;
    this.quant = quant;
    this.size = hSamp * vSamp;
  }

  JPEGComponent transformedBy (CartesianTransform transform)
  {
    JPEGComponent transformed = (JPEGComponent) clone ();
    if (transform.swaps ())
      {
        transformed.vSamp = hSamp;
        transformed.hSamp = vSamp;
      }
    return transformed;
  }

  void mark (int scanStart,int scanLimit)
  {
    for (int i = scanStart;i < scanLimit;i++)
      {
        Assertions.expect (!done [i]);
        done [i] = true;
      }
  }

  boolean isComplete ()
  {
    for (int i = 0;i < DCTsize;i++)
      if (!done [i])
        return false;
    return true;
  }

  boolean shapedLike (JPEGComponent c)
  {
    return hSamp == c.hSamp && vSamp == c.vSamp;
  }
}
