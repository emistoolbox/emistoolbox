/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.jpeg;

class ZigZag implements JPEGSpeaker
{
  final static int [] zigZag = new int [DCTsize]; // linear -> x,y
  final static int [] zagZig = new int [DCTsize]; // x,y -> linear
  static {
    boolean toggle = true;
    for (int sum = 0,index = 0;index < DCTsize;sum++,toggle = !toggle)
      for (int i = 0;i <= sum;i++)
        {
          int x = toggle ? i : sum - i;
          int y = toggle ? sum - i : i;
          if (x < DCTlength && y < DCTlength)
            {
              zagZig [zigZag [index] = y * DCTlength + x] = index;
              index++;
            }
        }
  }
}
