/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.font;

public class InvalidGlyphException extends RuntimeException
{
  public InvalidGlyphException () {}
  public InvalidGlyphException (String message)
  {
    super (message);
  }
}
