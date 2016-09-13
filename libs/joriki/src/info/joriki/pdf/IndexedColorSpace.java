/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

import java.awt.image.ColorModel;

import info.joriki.awt.image.FancyPaletteColorModel;
import info.joriki.util.General;
import info.joriki.util.Options;

public class IndexedColorSpace extends DerivedColorSpace
{
  final static float [] defaultColor = {0};

  float [] [] colors;

  IndexedColorSpace (PDFColorSpace base,int ncol,PDFObject lookup)
  {
    super (base,defaultColor);
    colors = new float [ncol] [];
    try {
      /*
        The base color space is used here with its default decode array.
        If it is a device color space, the values read here will not be
        remapped. I hope this is what the spec means by "Color values in
        the original device color space are passed unchanged to the
        default color space" on p. 178.
      */
      InputStream in = lookup instanceof PDFStream ?
        ((PDFStream) lookup).getInputStream ("3.4") :
        new ByteArrayInputStream (((PDFString) lookup).getBytes ());
      new PDFColorReader (in,base,false,8,null).read (colors);
    } catch (IOException ioe) {
      ioe.printStackTrace ();
      throw new Error ("couldn't read data for indexed color space");
    }
  }

  ColorModel getColorModel ()
  {
    int [] pixels = new int [colors.length];
    for (int i = 0;i < colors.length;i++)
      pixels [i] = baseColorSpace.mappedBase.toPixel (colors [i]);
    return new FancyPaletteColorModel (baseColorSpace.mappedBase.getColorModel (),pixels);
  }

  int toPixel (float [] color)
  {
    int pixel = (int) color [0];
    if (pixel < 0 || pixel >= colors.length) {
      Options.warn ("color index out of bounds");
      pixel = General.clip (pixel,0,colors.length - 1);
    }
    return pixel;
  }

  public float [] getBaseColor (float [] color) {
    return colors [toPixel (color)];
  }
}
