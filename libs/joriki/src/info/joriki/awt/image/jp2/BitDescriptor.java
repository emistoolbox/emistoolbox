/*
 * Copyright 2007 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.jp2;

import info.joriki.util.NotTestedException;

public class BitDescriptor {
  final static int SIGNED = 0x80;

  int depth;
  boolean signed;

  BitDescriptor (int code) {
    depth  = (code & ~SIGNED) + 1;
    signed = (code & SIGNED) != 0;
    if (signed)
      throw new NotTestedException ("signed samples");
  }

  public boolean equals (Object object) {
    if (!(object instanceof BitDescriptor))
	return false;
    BitDescriptor bitDescriptor = (BitDescriptor) object;
    return bitDescriptor.depth == depth && bitDescriptor.signed == signed;
  }
}