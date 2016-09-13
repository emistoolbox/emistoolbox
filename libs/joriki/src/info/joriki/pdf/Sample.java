/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

/* compareTo ordering inconsistent with equals */
public class Sample implements Comparable
{
  public float x;
  public float [] y;

  Sample (float x,float [] y)
  {
    this.x = x;
    this.y = y;
  }

  public int compareTo (Object o)
  {
    Sample sample = (Sample) o;
    return x == sample.x ? 0 : (x > sample.x ? 1 : -1);
  }

  public boolean equals (Object o)
  {
    if (!(o instanceof Sample))
      return false;
    Sample s = (Sample) o;
    if (x != s.x || y.length != s.y.length)
      return false;
    for (int i = 0;i < y.length;i++)
      if (y [i] != s.y [i])
        return false;
    return true;
  }

  public String toString ()
  {
    StringBuilder stringBuilder = new StringBuilder ();
    stringBuilder.append (x).append (" :");
    
    for (int i = 0;i < y.length;i++)
      stringBuilder.append (" ").append (y [i]);

    return stringBuilder.toString ();
  }
}
