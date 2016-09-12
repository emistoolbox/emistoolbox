/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.util;

public class Flags {
  private int flags;
  private int used;

  public Flags ()
  {
    this (0);
  }
  
  public Flags (int flags)
  {
    this.flags = flags;
  }
  
  public void setFlag (int bit)
  {
    flags |= 1 << bit;
  }
  
  public void clearFlag (int bit)
  {
    flags &= ~(1 << bit);
  }
  
  public boolean getFlag (int bit)
  {
    int mask = 1 << bit;
    used |= mask;
    return (flags & mask) != 0;
  }

  public void checkUnused (String table)
  {
    checkUnused (table,false);
  }
  
  public void checkUnused (String table,boolean set)
  {
    int reserved = (set ? ~flags : flags) & ~used; 
    if (reserved != 0)
      throw new NotImplementedException ("flags " + Integer.toBinaryString (reserved) + " in Table " + table);
  }
}
