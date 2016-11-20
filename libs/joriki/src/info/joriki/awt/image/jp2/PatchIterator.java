/*
 * Copyright 2003 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.jp2;

import java.awt.Dimension;
import java.awt.Rectangle;

class PatchIterator
{
  Rectangle container;
  Dimension patchSize;
  Rectangle patch;
  boolean clip;

  int xNext;
  int yNext;

  PatchIterator (Rectangle container,Dimension patchSize,boolean clip)
  {
    this.container = container;
    this.patchSize = patchSize;
    this.clip = clip;
    
    patch = new Rectangle (patchSize);
  }

  Rectangle nextPatch ()
  {
    patch.x = container.x + xNext;
    patch.y = container.y + yNext;
    if ((xNext += patchSize.width) >= container.width)
      {
	xNext = 0;
	yNext += patchSize.height;
      }
    return clip ? patch.intersection (container) : patch;
  }

  final int getWidth ()
  {
    return (container.width + patchSize.width - 1) / patchSize.width;
  }

  final int getHeight ()
  {
    return (container.height + patchSize.height - 1) / patchSize.height;
  }

  final int getCount ()
  {
    return getWidth () * getHeight ();
  }
}
