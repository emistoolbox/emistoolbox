/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.io.IOException;
import java.io.InputStream;

import info.joriki.awt.image.ImageBuffer;
import info.joriki.awt.image.ImageDecoder;
import info.joriki.awt.image.jp2.JP2Decoder;
import info.joriki.awt.image.jpeg.JPEGDecoder;
import info.joriki.awt.image.jpeg.JPEGReader;
import info.joriki.util.Assertions;
import info.joriki.util.NotImplementedException;
import info.joriki.util.NotTestedException;

public class PDFImage extends PDFColorReader implements DeviceColorSpaces
{
  public int width;
  public int height;
  public int length;
  public int npixels;
  public boolean isJP2;
  public boolean isJPEG;
  public boolean isInline;
  public boolean softMask;
  public boolean imageMask;
  public PDFImage maskImage;
  public boolean interpolate;
  public PDFDictionary dictionary;

  public PDFImage (PDFStream stream,ResourceResolver resourceResolver) throws IOException {
    this (stream,resourceResolver.map (resourceResolver.getColorSpace (stream.get ("ColorSpace"))));
  }
  
  PDFImage (PDFStream stream,PDFColorSpace colorSpace) throws IOException
  {
    this (stream.getInputStream (),stream.getLength (),stream,colorSpace);
    Assertions.expect (stream.isOptionallyOfType ("XObject"));
    Assertions.expect (stream.isOfSubtype ("Image"));
    stream.checkOPI ();
    stream.checkUnused ("4.36"); // inline images are checked in ContentStreamParser
  }

  // length is length of the image stream,
  // or sequence number for inline image.
  // the color space is already remapped.
  PDFImage (InputStream in,int length,
	    PDFDictionary dictionary,
	    PDFColorSpace colorSpace) throws IOException
  {
    super (in,colorSpace != null ? colorSpace : deviceColorSpaces [GRAY],
     dictionary.matchesOrContains ("Filter","CCITTFaxDecode") ||
     dictionary.matchesOrContains ("Filter","JPXDecode"),
	   dictionary.getInt ("BitsPerComponent",1),
	   dictionary.getFloatArray ("Decode"));
    this.dictionary = dictionary;
    this.length = length;
    width = dictionary.getInt ("Width");
    height = dictionary.getInt ("Height");
    npixels = width * height;
    imageMask = dictionary.getBoolean ("ImageMask",false);
    interpolate = dictionary.getBoolean ("Interpolate",false);
    isJPEG = dictionary.matchesOrContains ("Filter","DCTDecode");
    isJP2  = dictionary.matchesOrContains ("Filter","JPXDecode");

    if (isJP2 && colorSpace == null)
      // when implementing this, note the comment for the cmap box in JP2Decoder
      throw new NotImplementedException ("color space from JPEG2000 data");

    if (imageMask)
    {
      // we want 0 for transparent and 1 for opaque
      decodingParameters [0] = decode [1];
      decodingParameters [1] *= -1;
    }

    Assertions.expect (imageMask,colorSpace == null);
    if (imageMask)
      Assertions.expect (bitsPerComponent,1);
    else if (!isJP2)
      Assertions.expect (dictionary.contains ("BitsPerComponent"));

    PDFStream softMaskStream = (PDFStream) dictionary.get ("SMask");
    if (softMaskStream != null)
    {
      Assertions.expect (!imageMask);
      Assertions.expect (!softMaskStream.contains ("Mask"));
      Assertions.expect (!softMaskStream.contains ("SMask"));
      Assertions.expect (softMaskStream.contains ("BitsPerComponent"));
      Assertions.expect (softMaskStream.getName ("ColorSpace"),"DeviceGray");
      maskImage = new PDFImage (softMaskStream,deviceColorSpaces [GRAY]);
      Assertions.expect (!maskImage.imageMask);
      maskImage.softMask = true;
      matteColor = softMaskStream.getFloatArray ("Matte");
      if (matteColor != null)
      {
        // insidetv20040906-ae_1.pdf
        Assertions.expect (maskImage.width,width);
        Assertions.expect (maskImage.height,height);
        if (colorSpace instanceof IndexedColorSpace)
          throw new NotImplementedException ("matte color for indexed color space");
      }
    }
    else
    {
      PDFObject mask = dictionary.get ("Mask");
      if (imageMask)
        Assertions.expect (mask,null);
      else if (mask instanceof PDFStream)
      {
        PDFStream maskStream = (PDFStream) mask;
        maskImage = new PDFImage (maskStream,(PDFColorSpace) null);
        Assertions.expect (maskImage.imageMask);
        Assertions.expect (maskStream.isOfSubtype ("Image"));
        Assertions.expect (!maskStream.contains ("ColorSpace"));
        Assertions.expect (!maskStream.contains ("Mask"));
        Assertions.expect (!maskStream.contains ("SMask"));
      }
      else if (mask instanceof PDFArray)
      {
        maskArray = ((PDFArray) mask).toIntArray ();
        Assertions.expect (maskArray.length,ncomponents << 1);
      }
      else if (mask != null)
        throw new Error ("unknown mask " + mask.getClass ());
    }
    
    if (isJPEG)
      Assertions.expect (bitsPerComponent,8);
    
    // We've only encountered bit depth 1 in isixsigma20071112_31.pdf for uniform JPEG2000
    // images with a trivial palette, and bit depth 4 in isixsigma20071112_34.pdf,
    // also with a palette. If we encounter non-byte non-indexed JPEG 2000 images,
    // check all uses of bitsPerComponent to make sure there are no invalid assumptions.
    if (isJP2 && bitsPerComponent != 8 && !(colorSpace instanceof IndexedColorSpace))
      throw new NotTestedException ("non-byte non-indexed JPEG2000 image");

    if (isJPEG || isJP2)
    {
      Assertions.limit (ncomponents,1,4);
      Assertions.expect (maskArray,null);
      Assertions.expect (!imageMask);
    }
  }
  
  // replaces JPEG and JP2 input streams by raw image data streams
  // if the image is a JPEG, returns a JPEGReader for it, and decodes
  // it only if preserveJPEG is false
  public JPEGReader decodeImageFormats (boolean preserveJPEG) throws IOException {
    if (isJPEG) {
      // Note that jdapimin.c in the ghostscript JPEG code
      // uses color transform 2 as the default.
      JPEGReader reader = new JPEGReader (in,getColorTransform ());
      if (!preserveJPEG)
        decode (reader);
      return reader;
    }
    if (isJP2)
      decode (new JP2Decoder (in));
    return null;
  }

  public void decode (JPEGReader reader) {
    // We used to perform linear interpolation unless the image was meant for
    // immediate recompression as a JPEG with lower-resolution chromaticity.
    // However, it turns out that the Reader uses constant up-sampling, though
    // everything else (Firefox, IE, ghostview, GIMP, Paint) uses
    // linear interpolation. As usual, we emulate the Reader.
    JPEGDecoder decoder = new JPEGDecoder (false,false);
    decoder.setSource (reader);
    decode (decoder);
  }
  
  // This is inefficient, since it decodes the whole
  // file into an array before processing it,
  // but there's currently no elegant way around this
  // because two image decoders can't be concatenated,
  // since they can't be cranked.
  private void decode (ImageDecoder decoder) {
    ImageBuffer imageBuffer = new ImageBuffer ();
    decoder.startProduction (imageBuffer);
    in = imageBuffer.getInputStream ();
  }
  
  public boolean isInline () {
    return length < 0;
  }

  public int getColorTransform () {
    return dictionary.getInt ("ColorTransform",ncomponents == 3 ? 1 : 0);
  }
}
