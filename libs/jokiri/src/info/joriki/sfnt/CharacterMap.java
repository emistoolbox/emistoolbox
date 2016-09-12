/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.sfnt;

import java.util.Map;

public interface CharacterMap
{
  Map getMap ();
  int getGlyphIndex (int code);
}
