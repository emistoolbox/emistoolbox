/*
 * Copyright 2006 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.util;

public class Interval {
  public double min = Double.POSITIVE_INFINITY;
  public double max = Double.NEGATIVE_INFINITY;

  public void add (double x) {
    if (x < min)
      min = x;
    if (x > max)
      max = x;
  }
  
  public boolean contains (double x) {
    return min <= x && x <= max;
  }

  public boolean isEmpty () {
    return min > max;
  }

  public double length () {
    return max - min;
  }
  
  public void intersect (Interval interval) {
    intersect (interval.min,interval.max);
  }
  
  public void intersect (double min,double max) {
    if (this.min < min)
      this.min = min;
    if (this.max > max)
      this.max = max;
  }
  
  public void setToRealLine () {
    min = Double.MIN_VALUE;
    max = Double.MAX_VALUE;
  }
  
  public String toString () {
    return "[" + min + "," + max + "]";
  }
}
