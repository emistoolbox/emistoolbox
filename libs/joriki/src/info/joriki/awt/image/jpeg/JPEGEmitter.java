/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.jpeg;

import java.io.IOException;

import info.joriki.io.filter.Crank;

import info.joriki.awt.image.ImageDecoder;

class JPEGEmitter extends JPEGTerminal implements Crank
{
  JPEGFormat format;
  MCULayout layout;
  JPEGRequest request;
  ImageDecoder decoder;

  public void setSink (Object sink)
  {
    this.decoder = (ImageDecoder) sink;
  }

  public void setFormat (JPEGFormat format)
  {
    this.format = format;
    layout = format.layout;
    request = new JPEGRequest (PIXELS);
  }

  public int crank () throws IOException
  {
    int eod = source.readRequest (request);
    if (eod != OK)
      return eod;
    
    int x = request.x * layout.width;
    int y = request.y * layout.height;
    int w = Math.min (format.pixelWidth - x,layout.width);
    int h = Math.min (format.pixelHeight - y,layout.height);
    MCU mcu = format.mcus [request.y] [request.x];
    if (layout.mono)
      decoder.setPixels (x,y,w,h,mcu.byteData,layout.width);
    else
      decoder.setPixels (x,y,w,h,mcu.pixelData,layout.width);
    source.deallocationRequest (request);
    if (++request.x == format.width)
      {
        request.x = 0;
        request.y++;
      }

    return OK;
  }
}
