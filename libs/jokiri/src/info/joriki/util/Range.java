/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.util;

public class Range
{
  public int beg;
  public int end;

  public Range () {
    clear ();
  }
  
  public Range (int beg,int end)
  {
    this.beg = beg;
    this.end = end;
  }

  public void clear () {
    beg = Integer.MAX_VALUE;
    end = Integer.MIN_VALUE;
  }
  
  public void add (int val) {
    if (val < beg)
      beg = val;
    if (val > end)
      end = val;
  }
  
  public void copy(Range range) {
    beg = range.beg;
    end = range.end;
  }

  public boolean contains (int val)
  {
    return beg <= val && val <= end;
  }

  public boolean overlaps (Range range)
  {
    return contains (range.beg) || range.contains (beg);
  }

  
  public String toString ()
  {
    return "[" + beg + "," + end + "]";
  }
}
