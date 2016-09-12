/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

// this isn't derived from PDFColorSpace!

public class BaseColorSpace
{
  public PDFColorSpace base;
  public PDFColorSpace mappedBase;

  BaseColorSpace (PDFColorSpace base)
  {
    if (base instanceof PatternColorSpace)
      throw new IllegalArgumentException
        ("can't use pattern color space as base color space");
    
    this.base = base;
  }

  void map (ResourceResolver resourceResolver)
  {
    mappedBase = resourceResolver.map (base);
  }
}
