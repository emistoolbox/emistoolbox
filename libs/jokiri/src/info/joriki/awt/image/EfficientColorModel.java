/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image;

import java.awt.image.ColorModel;

abstract public class EfficientColorModel extends ColorModel
{
  public boolean hasAlpha;

  int ncomponents;
  int alphaShift;

  protected EfficientColorModel (int ncomponents,boolean hasAlpha)
  {
    super ((hasAlpha ? ncomponents + 1 : ncomponents) << 3);
    this.ncomponents = ncomponents;
    this.hasAlpha = hasAlpha;
    this.alphaShift = ncomponents << 3;
  }

  public int getRed (int pixel)
  {
    return (getOpaqueRGB (pixel) >> 16) & 0xff;
  }

  public int getGreen (int pixel)
  {
    return (getOpaqueRGB (pixel) >>  8) & 0xff;
  }

  public int getBlue (int pixel)
  {
    return (getOpaqueRGB (pixel) >>  0) & 0xff;
  }

  public int getAlpha (int pixel)
  {
    return hasAlpha ? pixel >>> alphaShift : 0xff;
  }

  public int getRGB (int pixel)
  {
    return (getAlpha (pixel) << 24) | getOpaqueRGB (pixel);
  }

  public int getTransparency ()
  {
    return hasAlpha ? TRANSLUCENT : OPAQUE;
  }

  public int getNumColorComponents ()
  {
    return ncomponents;
  }

  public int getNumComponents ()
  {
    return hasAlpha ? ncomponents + 1 : ncomponents;
  }

  // getDataElement and getComponents translate
  // ARGB in pixel ints to BGRA in component array
  // they only need to be consistent with each other
  // and put A last in the arrays for the quantizer

  public int getDataElement (int [] components,int offset) {
    int pixel = 0;
    int index = offset + getNumComponents ();
    while (index > offset) {
      pixel <<= 8;
      pixel |= components [--index];
    }
    return pixel;
  }

  public int [] getComponents (int pixel,int [] components,int offset) {
    int limit = offset + getNumComponents ();
    if (components == null)
      components = new int [limit];
    while (offset < limit) {
      components [offset++] = pixel & 0xff;
      pixel >>>= 8;
    }
    return components;
  }

  public int [] getComponentSize () {
    int [] componentSizes = new int [getNumComponents ()];
    for (int i = 0;i < componentSizes.length;i++)
      componentSizes [i] = 8;
    return componentSizes;
  }

  abstract protected int getOpaqueRGB (int pixel);
  abstract public boolean equals (Object o);
}
