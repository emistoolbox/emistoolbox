/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

abstract public class DerivedColorSpace extends PDFColorSpace implements Cloneable
{
  public BaseColorSpace baseColorSpace;

  DerivedColorSpace (PDFColorSpace base,float [] defaultColor)
  {
    super (defaultColor);
    baseColorSpace = new BaseColorSpace (base);
  }
  
  public DerivedColorSpace clone (PDFColorSpace base) {
    try {
      DerivedColorSpace clone = (DerivedColorSpace) super.clone ();
      clone.baseColorSpace = new BaseColorSpace (base);
      clone.baseColorSpace.mappedBase = clone.baseColorSpace.base;
      return clone;
    } catch (CloneNotSupportedException cnse) {
      throw new InternalError ();
    }
  }
  
  float [] toRGBArray (float [] color)
  {
    return baseColorSpace.mappedBase.toRGBArray (getBaseColor (color));
  }

  public PDFColorSpace getBaseColorSpace () {
    return baseColorSpace.mappedBase;
  }
  
  abstract public float [] getBaseColor (float [] color);
}
