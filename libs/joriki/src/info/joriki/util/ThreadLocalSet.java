/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.util;

import java.util.HashSet;
import java.util.Set;

public class ThreadLocalSet extends ThreadLocal {
  protected Object initialValue () {
    return new HashSet ();
  }
  
  public boolean add (Object o)
  {
    return ((Set) get ()).add (o);
  }

  public boolean contains (Object o)
  {
    return ((Set) get ()).contains (o);
  }
}
