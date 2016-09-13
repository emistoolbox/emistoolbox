/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.jpeg;

import java.io.IOException;

import info.joriki.io.filter.Crank;

import info.joriki.awt.image.ImageDecoder;

class JPEGSender extends JPEGTerminal implements Crank
{
  JPEGFormat format;
  MCULayout layout;
  JPEGRequest request;
  ImageDecoder decoder;

  int [] pixels;
  byte [] bytes;
  int scanLength;

  public void setSink (Object sink)
  {
    this.decoder = (ImageDecoder) sink;
  }

  public void setFormat (JPEGFormat format)
  {
    this.format = format;
    layout = format.layout;
    request = new JPEGRequest (PIXELS,format.width,1);
    scanLength = layout.width * format.width;
    int nsample = layout.height * scanLength;
    if (layout.mono)
      bytes = new byte [nsample];
    else
      pixels = new int [nsample];
  }

  public int crank () throws IOException
  {
    int eod = source.readRequest (request);
    if (eod != OK)
      return eod;

    for (int mx = 0;mx < format.width;mx++)
      {
        MCU mcu = format.mcus [request.y] [mx];
        for (int y = 0,k = 0;y < layout.height;y++)
          for (int x = 0;x < layout.width;x++,k++)
            {
              int index = (y * format.width + mx) * layout.width + x;
              if (layout.mono)
                bytes [index] = mcu.byteData [k];
              else
                pixels [index] = mcu.pixelData [k];
            }
      }
    int y = request.y * layout.height;
    int h = Math.min (format.pixelHeight - y,layout.height);
    if (layout.mono)
      decoder.setPixels (y,h,bytes,scanLength);
    else
      decoder.setPixels (y,h,pixels,scanLength);
    source.deallocationRequest (request);
    request.y++;
    return OK;
  }
}
