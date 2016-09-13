/*
 * Copyright 2005 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.util;

// replacements for Character methods introduced in 1.5
public class Unicode {
  final static int mask = 0x3FF;
  
  private Unicode () {}
  
  public static final char [] toChars (int codePoint) {
    return codePoint < 0x10000 ?
      new char [] {(char) codePoint} :
      new char [] {(char) (0xD800 | ((codePoint - 0x10000) >> 10)),
                   (char) (0xDC00 | ((codePoint & mask)))};
  }
  
  public static final int toCodePoint (char high,char low) {
    return ((high & mask) << 10) + 0x10000 + (low & mask);
  }
  
  public static final boolean isSurrogatePair (char high,char low) {
    return isHighSurrogate (high) && isLowSurrogate (low);
  }
  
  public static final boolean isLowSurrogate (char c) {
    return 0xDC00 <= c && c <= 0xDFFF;
  }
  
  public static final boolean isHighSurrogate (char c) {
    return 0xD800 <= c && c <= 0xDBFF;
  }
}
