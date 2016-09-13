/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.sfnt;

import info.joriki.io.Util;
import info.joriki.io.Outputable;

abstract public class SFNTTable implements SFNTSpeaker, Outputable
{
  protected String id;

  protected SFNTTable (String id)
  {
    this.id = id;
  }

  public byte [] toByteArray ()
  {
    return Util.toByteArray (this);
  }
}
