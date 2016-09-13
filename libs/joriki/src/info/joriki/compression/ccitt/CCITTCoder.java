/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.compression.ccitt;

import info.joriki.util.Assertions;

import info.joriki.compression.huffman.HuffmanCode;

public class CCITTCoder implements CCITTSpeaker
{
  int columns;

  int [] referenceChanges;
  int [] scanlineChanges;

  // these are always the same, but they're probably not used
  // often enough to warrant making them static
  HuffmanCode modeCode;
  HuffmanCode [] runlengthCodes = new HuffmanCode [2];

  CCITTCoder (int columns)
  {
    this.columns = columns;

    // allocate memory for change indices for one row, at most one per pixel
    // not sure exactly how many extra we need; 4 should be safe
    referenceChanges = new int [columns + 4];
    scanlineChanges = new int [columns + 4];

    modeCode = new HuffmanCode (modeCodes,null);

    Assertions.expect
      (terminatingCodes [0].length == terminatingCodes [1].length);
    Assertions.expect
      (makeupCodes [0].length == makeupCodes [1].length);

    int terminatingLength = terminatingCodes [0].length;
    int makeupLength = makeupCodes [0].length;
    int commonMakeupLength = commonMakeupCodes.length;
    
    int length = terminatingLength + makeupLength + commonMakeupLength + 1;
      
    int [] symbols = new int [length];

    int index = 0;
    for (int i = 0;i < terminatingLength;i++)
      symbols [index++] = i;
    for (int i = 0;i < makeupLength + commonMakeupLength;i++)
      symbols [index++] = terminatingLength + (i << 6);
    symbols [index] = EOL_TERM;

    for (int j = 0;j < 2;j++)
      {
        String [] codes = new String [length];
        index = 0;
        for (int i = 0;i < terminatingLength;i++)
          codes [index++] = terminatingCodes [j] [i];
        for (int i = 0;i < makeupLength;i++)
          codes [index++] = makeupCodes [j] [i];
        for (int i = 0;i < commonMakeupLength;i++)
          codes [index++] = commonMakeupCodes [i];
        codes [index] = EOL_CODE;
        runlengthCodes [j] = new HuffmanCode (codes,symbols);
      }
  }
}
