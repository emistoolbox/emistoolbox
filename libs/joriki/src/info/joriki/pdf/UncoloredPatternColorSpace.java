/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

/**
 * An uncolored pattern color space is a color space for an uncolored pattern.
 * It is {\em not} an uncolored color space for a pattern -- that is what a
 * {\em colored} pattern color space is! Slightly confusing...
 * @see ColoredPatternColorSpace
 */
public class UncoloredPatternColorSpace extends PatternColorSpace
{
  public BaseColorSpace baseColorSpace;

  UncoloredPatternColorSpace (PDFColorSpace base)
  {
    super (base.ncomponents);
    baseColorSpace = new BaseColorSpace (base);
  }
}
