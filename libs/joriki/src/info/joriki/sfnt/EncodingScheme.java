/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.sfnt;

import info.joriki.util.ConsistentlyComparable;

public class EncodingScheme extends ConsistentlyComparable
{
  public short platform;
  public short encoding;
  public short language;

  public EncodingScheme () {}

  public EncodingScheme (EncodingScheme s)
  {
    this.platform = s.platform;
    this.encoding = s.encoding;
    this.language = s.language;
  }

  public EncodingScheme (int platform,int encoding,int language)
  {
    this.platform = (short) platform;
    this.encoding = (short) encoding;
    this.language = (short) language;
  }

  public int compareTo (Object o)
  {
    EncodingScheme s = (EncodingScheme) o;
    if (platform != s.platform)
      return platform - s.platform;
    if (encoding != s.encoding)
      return encoding - s.encoding;
    if (language != s.language)
      return language - s.language;
    return 0;
  }

  public String toString ()
  {
    return platform + "/" + encoding + "/" + language;
  }
}
