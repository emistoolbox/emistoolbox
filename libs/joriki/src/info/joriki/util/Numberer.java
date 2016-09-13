/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.util;

import java.util.Map;
import java.util.HashMap;

public class Numberer<T>
{
  Map<T,Integer> map = new HashMap<T,Integer> ();

  int count;

  public Numberer ()
  {
    this (0);
  }

  public Numberer (int first)
  {
    count = first;
  }

  public boolean contains (T t)
  {
    return map.get (t) != null;
  }

  public int numberFor (T t)
  {
    return numberFor (t,null);
  }

  public int numberFor (T t,Handler<T> handler)
  {
    Integer number = map.get (t);
    if (number == null) {
      number = new Integer (count++);
      map.put (t,number);
      if (handler != null)
        handler.handle (t);
    }
    return number;
  }
}
