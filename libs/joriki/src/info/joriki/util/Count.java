/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.util;

public class Count extends CloneableObject implements Comparable
{
  public int count;

  public Count () {}
  public Count (int count) { this.count = count; }

  final public void increment ()
  {
    count++;
  }

  final public void decrement ()
  {
    count--;
  }

  final public void add (Count c)
  {
    count += c.count;
  }

  public String toString ()
  {
    return Integer.toString (count);
  }

  public int compareTo(Object o) {
    return count - ((Count) o).count;
  }
}

