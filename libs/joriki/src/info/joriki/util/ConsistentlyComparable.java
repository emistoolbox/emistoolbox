/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.util;

abstract public class ConsistentlyComparable implements Comparable
{
  public boolean equals (Object o)
  {
    try {
      return compareTo (o) == 0;
    } catch (ClassCastException cce) {
      return false;
    }
  }
}
