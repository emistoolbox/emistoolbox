/*
 * Copyright 2003 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.adobe;

abstract public class AdobeSpeaker
{
  public final static byte REGULAR = 0;
  public final static byte SPACE = 1;
  public final static byte DELIMITER = 2;

  public final static byte [] ctype = new byte [256];
  static {
    // all initialized to REGULAR
    ctype [0] = ctype [9] = ctype [10] = ctype [12] = ctype [13] = ctype [32] =
      SPACE;
    ctype ['('] = ctype [')'] =
      ctype ['['] = ctype [']'] =
      ctype ['{'] = ctype ['}'] =
      ctype ['<'] = ctype ['>'] =
      ctype ['/'] = ctype ['%'] =
      DELIMITER;
  }
}
