/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

// Some things would be easier here if all other color spaces
// were derived from something like ComponentColorSpace, but
// that would require a whole lot of casting in other places --
// so I just fitted the pattern color spaces into the general
// scheme of things.
public class PatternColorSpace extends MarkerColorSpace
{
  PatternColorSpace (int ncomponents)
  {
    super (ncomponents);
  }
}
