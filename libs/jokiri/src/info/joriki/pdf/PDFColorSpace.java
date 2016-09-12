/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.io.InputStream;

import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;

import info.joriki.awt.image.ColorConversions;

import info.joriki.util.General;
import info.joriki.util.Options;
import info.joriki.util.Assertions;
import info.joriki.util.NotImplementedException;

public abstract class PDFColorSpace implements DeviceColorSpaces, PDFOptions
{
  public int ncomponents;
  public float [] defaultColor;
  float [] defaultDecode;
  ColorModel colorModel;

  protected PDFColorSpace (float [] defaultColor,ColorModel colorModel)
  {
    this (defaultColor);
    this.colorModel = colorModel;
  }

  protected PDFColorSpace (float [] defaultColor)
  {
    this.defaultColor = defaultColor;
    this.ncomponents = defaultColor.length;

    defaultDecode = new float [ncomponents << 1];

    // This pattern of [0,1] intervals is right for almost
    // all color spaces, but is overwritten by the constructors
    // for ICCBasedColorSpace and LabColorSpace, and not used
    // by IndexedColorSpace and PatternColorSpace.
    for (int i = 1;i < defaultDecode.length;i += 2)
      defaultDecode [i] = 1;
  }

  // the array returned by this method may be overwritten by subsequent calls
  abstract float [] toRGBArray (float [] color);

  int toRGB (float [] color)
  {
    return ColorConversions.pack (toRGBArray (color));
  }

  int toPixel (float [] color)
  {
    return ColorConversions.pack (color);
  }

  ColorModel getColorModel ()
  {
    return colorModel;
  }

  public String toHexString (float [] color)
  {
    return General.zeroPad (Integer.toHexString (toRGB (color)),6);
  }

  // return one of the color space type constants in java.awt.color.ColorSpace or -1
  // overwritten by ICCBasedColorSpace and LabColorSpace
  int getType () {
    String type = getClass ().getName ();
    if (type.indexOf ("Gray") != -1)
      return ColorSpace.TYPE_GRAY;
    if (type.indexOf ("RGB") != -1)
      return ColorSpace.TYPE_RGB;
    if (type.indexOf ("CMYK") != -1)
      return ColorSpace.TYPE_CMYK;
    return -1;
  }

  protected void checkBlackPoint (PDFDictionary dictionary) {
    PDFArray blackPoint = (PDFArray) dictionary.get ("BlackPoint");
    if (blackPoint != null) {
      Assertions.expect (blackPoint.size (),3);
      for (int i = 0;i < 3;i++)
        if (blackPoint.doubleAt (i) != 0)
          throw new NotImplementedException ("non-trivial black point");
    }
  }

  public PDFColorSpace getBaseColorSpace () {
    return this;
  }
  
  public static PDFColorSpace getInstance
    (PDFObject specification,ResourceResolver resourceResolver)
  {
    if (specification instanceof PDFArray)
      {
        PDFArray array = (PDFArray) specification;
        PDFName theName = (PDFName) array.get (0);
        if (array.size () == 1)
          return resourceResolver.getColorSpace (theName);
        String name = theName.getName ();
        PDFObject arg = array.get (1);
        if (name.equals ("Indexed"))
          {
            PDFColorSpace base =
              resourceResolver.getColorSpace (arg);
            if (base instanceof IndexedColorSpace)
              throw new IllegalArgumentException
                ("can't use indexed color space as base color space for another indexed color space");

            int hival = array.intAt (2);
            Assertions.limit (hival,0,255); 
            PDFObject lookup = array.get (3);
            return new IndexedColorSpace (base,hival + 1,lookup);
          }
        if (name.equals ("CalRGB"))
          return approximateColors.isSet () ?
            deviceColorSpaces [RGB] :
            new CalRGBColorSpace ((PDFDictionary) arg);
        if (name.equals ("CalGray"))
          return approximateColors.isSet () ?
            deviceColorSpaces [GRAY] :
            new CalGrayColorSpace ((PDFDictionary) arg);
        if (name.equals ("Lab"))
          return new LabColorSpace ((PDFDictionary) arg);
        if (name.equals ("ICCBased"))
          {
            PDFStream profileStream = (PDFStream) arg;
            int ncomponents = profileStream.getInt ("N");
            Assertions.limit (ncomponents,1,4);
            Assertions.unexpect (ncomponents,2);
            float [] range = profileStream.getFloatArray ("Range");
            try {
              InputStream in = profileStream.getInputStream ("4.16");
              if (!approximateColors.isSet ())
                return new ICCBasedColorSpace (ncomponents,in,range);
            } catch (Exception e) {
              // 1588247414.pdf, 0873378830.pdf have byte-order errors
              System.err.println ("error reading ICC color space specification -- using alternate color space");
            }
            
            if (range != null)
              throw new NotImplementedException ("Range specification on ICC-based color space");
            PDFObject alternateSpec = profileStream.get ("Alternate");
            PDFColorSpace alternate = alternateSpec == null ?
              deviceColorSpaces [ncomponents >> 1] :
              resourceResolver.getColorSpace (alternateSpec);
            Assertions.expect (alternate.ncomponents,ncomponents);
            Options.warn ("using alternate color space for ICC-based color space");
            return alternate;
          }
        if (name.equals ("Separation") || name.equals ("DeviceN"))
          {
            PDFArray colorants;
            if (name.equals ("Separation"))
              {
                PDFName colorant = (PDFName) arg;
                if (colorant.getName ().equals ("All"))
                  return new AllSeparationColorSpace ();
                colorants = new PDFArray ();
                colorants.add (colorant);
              }
            else
              // here All is not allowed
              colorants = (PDFArray) arg;

            boolean none = true;
            for (int i = 0;i < colorants.size ();i++)
              none &= ((PDFName) colorants.get (i)).getName ().equals ("None");
            if (none)
              return new NoneSeparationColorSpace ();
              
            PDFColorSpace base =
              resourceResolver.getColorSpace (array.get (2));
            if (base instanceof ColorantColorSpace ||
                base instanceof IndexedColorSpace)
              throw new IllegalArgumentException
                ("can't use special color space as base color space for colorant color space");
            PDFFunction tintTransform = (PDFFunction)
              resourceResolver.getCachedObject
              (ObjectTypes.FUNCTION,array.get (3));

            return new ColorantColorSpace (colorants,base,tintTransform);
          }
        if (name.equals ("Pattern"))
          return new UncoloredPatternColorSpace
            (resourceResolver.getColorSpace (arg));
    
        throw new NotImplementedException ("Array color space " + name);
      }
    throw new NotImplementedException ("Color space specification " + specification);
  }
}
