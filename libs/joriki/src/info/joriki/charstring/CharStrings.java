/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.charstring;

public class CharStrings
{
  public static int bias (int nsub)
  {
    if (nsub < 1240)
      return 107;
    if (nsub < 33900)
      return 1131;
    return 32768;
  }
}
