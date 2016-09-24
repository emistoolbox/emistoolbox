/*
 * Copyright 2006 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.util.diff;

abstract public class AbstractDiff implements Diff {
  protected byte [] a;
  protected byte [] b;
  boolean swap;
  int targetDiagonal;
  
  protected AbstractDiff (byte [] a,byte [] b) {
    swap = a.length > b.length;
    this.a = swap ? b : a;
    this.b = swap ? a : b;
    targetDiagonal = this.a.length - this.b.length;
  }
  
  protected int slide (int leftRow,int topRow,int diagonal) {
    int row = Math.max (leftRow,topRow + 1);
    while (row + diagonal < a.length && row < b.length && a [row + diagonal] == b [row])
      row++;
    return row;
  }
  
  static boolean contains (byte [] a,byte [] b) {
    for (int i = 0,j = 0;;i++) {
      if (j == b.length)
        return true;
      if (i == a.length)
        return false;
      if (a [i] == b [j])
        j++;
    }
  }
}
