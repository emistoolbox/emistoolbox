/*
 * Copyright 2006 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.util.diff;

import info.joriki.util.ByteArray;

public class Insertion {
  public int position;
  public ByteArray byteArray;
  
  public Insertion (int position,byte [] bytes,int beg,int end) {
    this.byteArray = new ByteArray (bytes,beg,end);
    this.position = position;
  }
  
  public boolean equals (Object o) {
    if (!(o instanceof Insertion))
      return false;
    Insertion i = (Insertion) o;
    return i.position == position && i.byteArray.equals (byteArray);
  }

  public String toString () {
    return "i " + position + " : '" + byteArray + "'";
  }
}
