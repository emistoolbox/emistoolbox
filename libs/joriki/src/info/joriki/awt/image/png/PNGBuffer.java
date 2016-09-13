/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.png;

import info.joriki.util.General;
import info.joriki.util.Assertions;
import info.joriki.util.NotImplementedException;

abstract public class PNGBuffer implements PNGSpeaker
{
  final static byte SUB_BYTE = 0;
  final static byte ONE_BYTE = 1;
  final static byte TWO_BYTE = 2;

  public byte [] data;
  public int dataOffset;
  public int bytesPerScanline;

  byte [] scanline;
  byte [] lastline;

  boolean hasAlpha;
  int filterOffset;
  int pixelsPerByte;
  int bitDepth;
  int bitShift;
  byte type;
  int pos;

  static byte paethPredictor (byte a,byte b,byte c)
  {
    int ia = a & 0xff;
    int ib = b & 0xff;
    int ic = c & 0xff;

    int p = ia + ib - ic;

    int da = p - ia;
    int db = p - ib;
    int dc = p - ic;

    da *= da;
    db *= db;
    dc *= dc;

    return (da <= db && da <= dc) ? a : (db <= dc ? b : c);
  }

  static byte averagePredictor (byte a,byte b)
  {
    return (byte) (((a & 0xff) + (b & 0xff)) >> 1);
  }

  protected PNGBuffer (PNGHeader header)
  {
    this (samplesPerType [header.colorType],header.bitDepth,header.width,header.colorType);
  }
  
  protected PNGBuffer (int samplesPerPixel,int bitDepth,int width,byte colorType)
  {
    this.bitDepth = bitDepth;
   
    if (colorType == GRAYSCALE_ALPHA)
      throw new NotImplementedException ("grayscale alpha");

    Assertions.unexpect (samplesPerPixel,0);
 
    int bitsPerPixel = samplesPerPixel * bitDepth;
    bytesPerScanline = General.bytesForBits (bitsPerPixel * width);
    filterOffset = General.bytesForBits (bitsPerPixel);

    int bytesPerBuffer = bytesPerScanline + filterOffset;

    lastline = new byte [bytesPerBuffer];
    scanline = new byte [bytesPerBuffer];
    
    hasAlpha = (colorType & ALPHA) == ALPHA;
    
    type = colorType == BYTES || bitDepth == 8 ? ONE_BYTE : bitDepth < 8 ? SUB_BYTE : TWO_BYTE;

    switch (type) {
    case SUB_BYTE :
      pixelsPerByte = 8 / bitDepth;
      Assertions.expect (bitDepth * pixelsPerByte,8);
      Assertions.unexpect (colorType,TRUECOLOR);
      if (colorType == GRAYSCALE)
        bitShift = 8 - bitDepth;
      data = new byte [pixelsPerByte * bytesPerScanline];
      dataOffset = 0;
      break;
    case ONE_BYTE :
      data = scanline;
      dataOffset = filterOffset;
      break;
    case TWO_BYTE :
      Assertions.unexpect (colorType,PALETTECOLOR);
      data = new byte [samplesPerPixel * width];
      dataOffset = 0;
      break;
    default :
      throw new InternalError ();
    }
  }

  final void switchBuffers ()
  {
    byte [] tmp = lastline;
    lastline = scanline;
    scanline = tmp;
    if (type == ONE_BYTE)
      data = scanline;
  }
}
