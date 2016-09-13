/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.charstring;

import info.joriki.font.GlyphProvider;

abstract public class Type1CharStringFont implements CharStringFont
{
  protected Type1CharStringDecoder charStringDecoder;
  public Type1CharStringDecoder getCharStringDecoder ()
  {
    if (charStringDecoder == null)
      charStringDecoder = new Type1CharStringDecoder (this);

    return charStringDecoder;
  }

  public GlyphProvider getGlyphProvider (Object glyph)
  {
    getCharStringDecoder ();
    charStringDecoder.setCharString (getCharString ((String) glyph));
    return charStringDecoder;
  }
}
