/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.util;

public class ThreadLocalCount extends ThreadLocal
{
  protected Object initialValue ()
  {
    return new Count ();
  }
  
  public int nextCount ()
  {    
    Count count = (Count) get ();
    count.increment ();
    return count.count;
  }
}
