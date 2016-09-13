/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.truetype;

public class GlyphPoint implements Flags
{
  public byte flags;
  public short [] x = new short [2];
  
  public final boolean isSet (byte flag)
  {
    return (flags & flag) == flag;
  }
  
  public String toString ()
  {
    return "(" + x [0] + "," + x [1] + ") " + (isSet (ONCURVE) ? "on" : "off");
  }
}

