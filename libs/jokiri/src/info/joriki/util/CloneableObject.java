/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.util;

public class CloneableObject implements Cloneable
{
  public Object clone ()
  {
    try { return super.clone (); } catch (CloneNotSupportedException cnse) { throw new InternalError (); }
  }
}
