/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.charstring;

import info.joriki.font.DescribedFont;

import info.joriki.adobe.DefaultEncodingProvider;

public interface CharStringFont extends DescribedFont, DefaultEncodingProvider
{
  byte [] getCharString (String glyph);
  byte [] [] getSubroutines ();
}
