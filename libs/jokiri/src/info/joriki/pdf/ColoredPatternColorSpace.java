/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

/**
 * A colored pattern color space is a color space for a colored pattern.
 * It is {\em not} a colored color space for a pattern -- that is what an
 * {\em uncolored} pattern color space is! Slightly confusing...
 * @see UncoloredPatternColorSpace
 */
public class ColoredPatternColorSpace extends PatternColorSpace
{
  ColoredPatternColorSpace ()
  {
    super (0);
  }
}
