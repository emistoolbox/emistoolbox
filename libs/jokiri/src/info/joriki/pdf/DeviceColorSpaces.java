/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

public interface DeviceColorSpaces
{
  final static int GRAY = 0;
  final static int RGB  = 1;
  final static int CMYK = 2;
  
  final static PDFColorSpace [] deviceColorSpaces = new PDFColorSpace [] {
    new DeviceGrayColorSpace (),
    new DeviceRGBColorSpace (),
    new DeviceCMYKColorSpace ()
      };

  final static String [] deviceColorSpaceSuffixes = {"Gray","RGB","CMYK"};
}
