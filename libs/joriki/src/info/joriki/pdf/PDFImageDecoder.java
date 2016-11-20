/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.awt.image.DataBuffer;
import java.io.IOException;

import info.joriki.awt.image.StaticImageDecoder;
import info.joriki.awt.image.UltimateImageConsumer;

import info.joriki.awt.image.jp2.JP2Decoder;

import info.joriki.awt.image.jpeg.JPEGReader;
import info.joriki.util.NotImplementedException;

public class PDFImageDecoder extends StaticImageDecoder
{
  PDFImage image;

  public PDFImageDecoder (PDFImage image)
  {
    setSource (image);
  }

  public void setSource (Object source)
  {
    this.image = (PDFImage) source;
  }

  public void produceStaticImage () throws IOException
  {
    width = image.width;
    height = image.height;
    colorModel = image.colorSpace.getColorModel ();

    if (image.bitsPerComponent < 8 &&
       (image.colorSpace instanceof DeviceGrayColorSpace ||
        image.colorSpace instanceof CalGrayColorSpace) &&
       (image.decode == null ||
       (image.decode [0] == 0 && image.decode [1] == 1) ||
       (image.decode [0] == 1 && image.decode [1] == 0)))
          setProperty (BITS_PER_COMPONENT,new Integer (image.bitsPerComponent));

    setParameters ();

    int transferType = colorModel.getTransferType();

    switch (transferType)
    {
    case DataBuffer.TYPE_BYTE:
      byte [] byteRow = new byte [width];
      for (int y = 0;y < height;y++)
      {
        image.read (byteRow);
        setPixels (y,byteRow);
      }
      break;
    case DataBuffer.TYPE_INT:
      int [] intRow = new int [width];
      for (int y = 0;y < height;y++)
      {
        image.read (intRow);
        setPixels (y,intRow);
      }
      break;
    default:
      throw new NotImplementedException ("transfer type " + transferType);
    }
  }
  
  public void discard () throws IOException
  {
    if (image.isJPEG)
      new JPEGReader (image.in).discard ();
    else if (image.isJP2)
    {
      new JP2Decoder (image.in).discard ();
      throw new info.joriki.util.NotTestedException ();
    }
    else
      startProduction (new UltimateImageConsumer ());
  }
}
