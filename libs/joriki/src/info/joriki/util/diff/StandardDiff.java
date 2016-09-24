/*
 * Copyright 2006 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.util.diff;

import info.joriki.util.Assertions;

import java.util.Arrays;

public class StandardDiff extends AbstractDiff {
  private int [] bestForwardRows;
  private int [] bestBackwardRows;

  public StandardDiff (byte [] a,byte [] b) {
    super (a,b);
    int ndiagonals = a.length  + b.length + 1;
    bestForwardRows = new int [ndiagonals];
    bestBackwardRows = new int [ndiagonals];
  }
  
  private byte [] lcs;
  
  private byte [] longestCommonSequence (int limit) {
    lcs = null;
    longestCommonSequence (0,a.length,0,b.length,0,0,limit);
    return lcs;
  }
  
  private void longestCommonSequence (int aoff,int alen,int boff,int blen,int loff,int llen,int limit) {
    if (lcs != null) {
      while (alen != 0 && blen != 0 && a [aoff] == b [boff]) {
        lcs [loff] = a [aoff];
        aoff++;
        boff++;
        loff++;
        alen--;
        blen--;
        llen--;
      }
      while (alen != 0 && blen != 0 && a [aoff + alen - 1] == b [boff + blen - 1]) {
        lcs [loff + llen - 1] = a [aoff + alen - 1];
        alen--;
        blen--;
        llen--;
      }
      if (llen == 0)
        return;
      Assertions.unexpect (alen,0);
      Assertions.unexpect (blen,0);
    }
    
    int maxx = Math.min (alen,limit);
    int maxy = blen + Math.min (0,limit - alen);
    int maxn = maxx + maxy;
    
    Arrays.fill (bestForwardRows ,blen - maxy,blen + maxx + 1,Integer.MIN_VALUE);
    Arrays.fill (bestBackwardRows,alen - maxx,alen + maxy + 1,Integer.MAX_VALUE);

    int bestRow;
    int diagonal;
    int offset = blen;
    int nForward = -1;
    int nBackward = -1;
  outer:
    for (;;) {
      for (int x = 0,y = ++nForward;y >= 0;x++,y--)
        if (x <= maxx && y <= maxy) {
          diagonal = x - y;
          bestRow = Math.max
          (x == 0 ? 0 : bestForwardRows [offset + diagonal - 1],
           y == 0 ? 0 : bestForwardRows [offset + diagonal + 1] + 1);
          if (bestRow >= bestBackwardRows [offset + diagonal])
            break outer;
          while (bestRow + diagonal < alen && bestRow < blen && a [aoff + bestRow + diagonal] == b [boff + bestRow])
            bestRow++;
          
          bestForwardRows [offset + diagonal] = bestRow;
        }

      if (nForward + nBackward == maxn)
        return;

      for (int x = 0,y = ++nBackward;y >= 0;x++,y--)
        if (x <= maxx && y <= maxy) {
          diagonal = (alen - x) - (blen - y);
          bestRow = Math.min
          (x == 0 ? blen : bestBackwardRows [offset + diagonal + 1],
           y == 0 ? blen : bestBackwardRows [offset + diagonal - 1] - 1);
          if (bestRow <= bestForwardRows [offset + diagonal])
            break outer;
          while (bestRow + diagonal > 0 && bestRow > 0 && a [aoff + bestRow + diagonal - 1] == b [boff + bestRow - 1])
            bestRow--;

          bestBackwardRows [offset + diagonal] = bestRow;
        }
      if (nForward + nBackward == maxn)
        return;
    }
    
    int length = (alen + blen - (nBackward + nForward - 1)) >> 1; 
    if (lcs == null) {
      loff = 0;
      llen = length;
      lcs = new byte [llen];
    }
    else
      Assertions.expect (llen,length);
    
    int bestColumn = bestRow + diagonal;
    int bestLength = (bestRow + bestColumn - nForward) >> 1;
    longestCommonSequence (aoff,bestColumn,boff,bestRow,loff,bestLength,nForward);
    longestCommonSequence (aoff + bestColumn,alen - bestColumn,boff + bestRow,blen - bestRow,loff + bestLength,llen - bestLength,nBackward);
  }
  
  public byte [] longestCommonSequence () {
    return longestCommonSequence (Integer.MAX_VALUE);
  }
}
