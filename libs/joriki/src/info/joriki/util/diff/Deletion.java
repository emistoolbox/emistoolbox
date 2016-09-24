/*
 * Copyright 2006 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.util.diff;

public class Deletion {
  public int position;
  public int end;

  public Deletion (int position,int length) {
    this.position = position;
    this.end = length;
  }
  
  public boolean equals (Object o) {
    if (!(o instanceof Deletion))
      return false;
    Deletion d = (Deletion) o;
    return d.position == position && d.end == end;
  }

  public String toString () {
    return "d [" + position + "," + end + "]";
  }
}
