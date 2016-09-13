/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.compression.lzw;

abstract class LZWCoder
{
  // determines how much earlier than strictly necessary
  // the code size is increased
  final int earlyChange;

  int nroot;    // the number of "letters"
  int initialCodeSize;
  int codeSize; // the current length in bits of a code
  int clearCode;
  int endCode;
  int nextCode;
  int maxCode;

  // the highest codeSize allowed
  final static int maxBits = 12;
  final static int hiCode  = (1 << maxBits) - 1;

  protected LZWCoder (int rootSize,int earlyChange)
  {
    this.earlyChange = earlyChange;
    init (rootSize + 1,1 << rootSize);
  }

  protected LZWCoder (int nroot)
  {
    earlyChange = 0;
    int max = nroot + 1;
    int bits = 0;
    while (max != 0)
      {
        bits++;
        max >>= 1;
      }
    init (bits,nroot);
  }
  
  private void init (int initialCodeSize,int nroot)
  {
    if (initialCodeSize > maxBits)
      throw new IllegalArgumentException ("initial code size must be less than 12");

    this.initialCodeSize = initialCodeSize;
    this.nroot = nroot;

    clearCode = nroot;
    endCode = nroot + 1;
  }

  void clearCodes ()
  {
    codeSize = initialCodeSize;
    maxCode = 1 << codeSize;
    nextCode = endCode + 1;
  }
}
