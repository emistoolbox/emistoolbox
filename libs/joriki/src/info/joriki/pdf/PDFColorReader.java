/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;

import java.util.Map;
import java.util.HashMap;

import info.joriki.io.NybbleInputStream;
import info.joriki.io.KnownStreamCorruptedException;

import info.joriki.util.Options;
import info.joriki.util.Assertions;
import info.joriki.util.NotImplementedException;

public class PDFColorReader implements PDFOptions
{
  final static float [] defaultIndexedDecodingParameters = {0,1};

  public InputStream in;
  public PDFColorSpace colorSpace;
  public int bitsPerComponent;
  public int ncomponents;
  public int [] maskArray;
  public byte [] maskPixels;      // output
  public byte [] softMaskPixels;  // input
  public float [] decode;
  public float [] matteColor;

  protected float [] decodingParameters;

  private float [] color;
  private Map colorCache;
  private int maskIndex = 0; // used for both and maskPixels and softMaskPixels, which are mutually exclusive
  private int key;

  PDFColorReader (InputStream in,PDFColorSpace colorSpace,boolean forceBytes,int bitsPerComponent,float [] decode)
  {
    if (bitsPerComponent > 8)
      // introduced in PDF 1.5
      // if we implement this, make sure PNGInputBuffer can handle it
      // it handles two-byte samples, but for PDF we use "color type" BYTES
      throw new NotImplementedException ("multi-byte color components");
    // forceBytes saves packing and unpacking CCITTFaxDecode bits and JPXDecode results
    this.in = forceBytes || bitsPerComponent == 8 ? in :
      new NybbleInputStream (in,bitsPerComponent,false);
    this.colorSpace = colorSpace;
    this.bitsPerComponent = bitsPerComponent;
    this.ncomponents = colorSpace.ncomponents;
    this.color = new float [ncomponents];
    this.decode = decode;

    if (cacheColors.isSet ())
      colorCache = new HashMap ();

    Assertions.expect (!(colorSpace instanceof PatternColorSpace));

    calculateDecodingParameters ();
  }

  private void calculateDecodingParameters ()
  {
    if (decode == null && colorSpace instanceof IndexedColorSpace)
      decodingParameters = defaultIndexedDecodingParameters;
    else
    {
      if (decode == null)
        decode = colorSpace.defaultDecode;
      if (decode.length != ncomponents << 1)
      {
        if (decode.length == 8 && ncomponents == 1)
          // Ghostscript source says AI8 produces these
          Options.warn ("invalid decode array");
        else
          throw new Error ("invalid decode array");
      }
      decodingParameters = new float [decode.length];
      for (int j = 0;j < decode.length;j += 2)
      {
        float dmin = decode [j];
        float dmax = decode [j+1];
        decodingParameters [j] = dmin;
        decodingParameters [j+1] = (dmax - dmin) / ((1 << bitsPerComponent) - 1);
      }
    }
  }

  boolean padding;

  private void readColor () throws IOException
  {
    /* There's a subtle reason for having sample outside the loop
       and not resetting it to 0 when we catch an exception --
       if we're reading RGB or CMYK values and the first component
       has already been read, possibly with a corrupted value,
       at least we'll extend it to gray and not to red or cyan. */
    key = 0;
    int sample = 0;
    byte maskPixel = 0;
    float alpha = matteColor == null ? 1 : (softMaskPixels [maskIndex++] & 0xff) / 255f;
    for (int i = 0,j = 0;i < ncomponents;i++,j += 2)
      {
        if (!padding)
          try {
            sample = in.read ();
            if (sample < 0)
              throw new StreamCorruptedException ("Premature end of color data");
          } catch (KnownStreamCorruptedException ksce) {
            Options.warn (ksce.getMessage () + " -- padding color data with zeros");
            padding = true;
          }
        
        if (maskArray != null && 
	    !(maskArray [j] <= sample && 
	      sample <= maskArray [j+1]))
          maskPixel = -1;

        key <<= 8;
        key += sample;

        color [i] = decodingParameters [j] + decodingParameters [j+1] * sample;

        if (alpha != 1)
        {
          float matte = matteColor [i];
          color [i] -= matte;
          if (alpha == 0) {
            // expected since color should be matte color,
            // desired since PNG spec says to "output black".
            if (color [i] != 0) {
              // allow sample value deviation of 2 for lossy JPEGs (churchpro200603-ae_82.pdf)
              float limit = 2 * decodingParameters [j+1];
              Assertions.limit (color [i],-limit,limit);
              color [i] = 0;
            }
          }
          else
          {  
            color [i] /= alpha;
            color [i] += matte;
            // PDF spec says results if this is out of range are undefined
          }
        }
      }

    if (maskPixels != null)
      maskPixels [maskIndex++] = maskPixel;
  }

  private int readPixel () throws IOException
  {
    readColor ();

    if (colorCache == null)
      return colorSpace.toPixel (color);

    Integer theKey = new Integer (key);
    Integer pixel = (Integer) colorCache.get (theKey);
    if (pixel == null)
      {
        pixel = new Integer (colorSpace.toPixel (color));
        colorCache.put (theKey,pixel);
      }

    return pixel.intValue ();
  }
  
  void read (float [] [] colors) throws IOException
  {
    for (int i = 0;i < colors.length;i++)
      {
        readColor ();
        colors [i] = color.clone ();
      }
    flush ();
  }
  
  void read (byte [] pixels) throws IOException
  {
    for (int i = 0;i < pixels.length;i++)
      pixels [i] = (byte) readPixel ();
    flush ();
  }
  
  void read (int [] pixels) throws IOException
  {
    for (int i = 0;i < pixels.length;i++)
      pixels [i] = readPixel ();
    flush ();
  }
  
  void flush ()
  {
    if (in instanceof NybbleInputStream)
      ((NybbleInputStream) in).flush ();
  }
}
