/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.InputStream;

import info.joriki.io.filter.BitBuffer;
import info.joriki.io.filter.Concatenator;
import info.joriki.io.filter.BytesToBitsConverter;

public class InputStreamBitSource extends BitBuffer
{
  public InputStreamBitSource (InputStream in,boolean lsbFirst)
  {
    super (lsbFirst);

    Concatenator.concatenate (new BytesToBitsConverter (),this).setSource (in);
  }

  public void byteAlign ()
  {
    dropBits (len & 7);
  }
}
