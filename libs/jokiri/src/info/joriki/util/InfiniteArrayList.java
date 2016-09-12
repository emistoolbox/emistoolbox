/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.util;

import java.util.ArrayList;

public class InfiniteArrayList<T> extends ArrayList<T>
{
  public T set (int index,T element)
  {
    while (size () <= index)
      add (null);
    return super.set (index,element);
  }

  public T get (int index)
  {
    return index < size () ? super.get (index) : null;
  }
}
