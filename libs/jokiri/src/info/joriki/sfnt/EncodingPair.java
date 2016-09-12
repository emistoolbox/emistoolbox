/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.sfnt;

public class EncodingPair
{
  public int code;
  public int glyphIndex;

  public EncodingPair (int code,int glyphIndex)
  {
    this.code = code;
    this.glyphIndex = glyphIndex;
  }
}
