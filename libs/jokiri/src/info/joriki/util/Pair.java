/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.util;

public class Pair<T,U>
{
  public T first;
  public U second;

  public Pair (T first,U second)
  {
    this.first = first;
    this.second = second;
  }

  public boolean equals (Object o)
  {
    if (!(o instanceof Pair))
      return false;
    Pair p = (Pair) o;
    return first.equals (p.first) && second.equals (p.second);
  }

  public int hashCode ()
  {
    return first.hashCode () + second.hashCode ();
  }

  public String toString ()
  {
    return '(' + first.toString () + ',' + second.toString () + ')';
  }
}
