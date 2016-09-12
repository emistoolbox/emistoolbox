/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.font;

public class FatalInvalidGlyphException extends InvalidGlyphException
{
  public FatalInvalidGlyphException () {}
  public FatalInvalidGlyphException (String message)
  {
    super (message);
  }
}
