/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.util;

abstract public class SimpleNumber extends Number
{
  public long longValue ()
  {
    throw new InternalError ();
  }
  
  public float floatValue ()
  {
    throw new InternalError ();
  }
}
