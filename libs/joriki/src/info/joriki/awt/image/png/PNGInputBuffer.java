/*
 * Copyright 2003 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.png;

import java.io.IOException;

import info.joriki.io.Util;
import info.joriki.io.Readable;

import info.joriki.util.Assertions;
import info.joriki.util.NotImplementedException;

public class PNGInputBuffer extends PNGBuffer implements Readable
{
  Readable in;
  byte filter;

  byte [] [] lookupTable;

  public PNGInputBuffer (PNGHeader header,Readable in)
  {
    super (header);
    init (in,VAR,header.colorType);
  }

  public PNGInputBuffer (int samplesPerPixel,int bitDepth,int width,
                         byte filter,boolean predictSamples,Readable in)
  {
    super (samplesPerPixel,bitDepth,width,BYTES);
    init (in,filter,BYTES);
    if (predictSamples && bitDepth != 8)
      throw new NotImplementedException ("sample prediction");
  }

  void init (Readable in,byte filter,byte colorType)
  {
    this.in = in;
    this.filter = filter;
    if (type == SUB_BYTE)
      {
        lookupTable = new byte [256] [pixelsPerByte];
        int mask = (1 << bitDepth) - 1;
        for (int i = 0;i < 256;i++)
          for (int j = pixelsPerByte - 1,k = 0;k < 8;j--,k += bitDepth)
            {
              int bits = (byte) ((i >> k) & mask);
              if (colorType == GRAYSCALE)
                bits = Math.round ((255f * bits) / mask);
              lookupTable [i] [j] = (byte) bits;
            }
      }
    pos = data.length;
  }

  public int readRow () throws IOException
  {
    switchBuffers ();

    int decodingFilter = filter;
    if (decodingFilter == VAR)
    {
      decodingFilter = in.read ();
      if (decodingFilter < 0)
        return decodingFilter;
    }
    Assertions.limit (decodingFilter,0,NFILTER-1);

    int read = Util.readMaximally (in,scanline,filterOffset,bytesPerScanline);

    if (read > 0)
    {  
      if (decodingFilter != NONE)
        for (int i = 0,j = filterOffset;i < read;i++,j++)
          scanline [j] += decodingFilter <= UP ?
          (decodingFilter == UP ? lastline [j] : scanline [i]) :
           decodingFilter == AVERAGE ?
           averagePredictor (lastline [j],scanline [i]) :
             paethPredictor (lastline [j],scanline [i],lastline [i]);

      switch (type) {
      case SUB_BYTE :
        for (int i = 0,j = filterOffset,index = 0;i < read;i++,j++,index += pixelsPerByte)
        {
          byte [] lookup = lookupTable [scanline [j] & 0xff];
          System.arraycopy (lookup,0,data,index,pixelsPerByte);
        }
        break;
      case ONE_BYTE :
        break;
      case TWO_BYTE :
        for (int i = 0,j = filterOffset;j < filterOffset + read;)
        {
          int word = scanline [j++] & 0xff;
          word = (word << 8) | (scanline [j++] & 0xff);
          data [i++] = (byte) ((word * 0xff + 0x7fff) / 0xffff);
        }
        break;
      default :
        throw new InternalError ();
      }
    }
    
    pos = dataOffset;
    
    return read;
  }
  
  public int read () throws IOException {
    if (pos == data.length) {
      int result = readRow ();
      if (result < 0)
        return result;
    }
    return data [pos++] & 0xff;
  }

  public void close () {
    throw new NotImplementedException ();
  }

  public int read (byte [] b) throws IOException {
    return read (b,0,b.length);
  }

  public int read (byte [] b,int off,int len) throws IOException {
    int read = 0;
    while (read < len) {
      if (pos == data.length)
        if (readRow () != bytesPerScanline)
          break;
      int n = Math.min (len - read,data.length - pos);
      System.arraycopy (data,pos,b,off + read,n);
      pos += n;
      read += n;
    }
    return read;
  }
  
  public int readRGBA () throws IOException {
    int rgb = 0;
    for (int i = 0;i < 3;i++) {
      rgb <<= 8;
      rgb |= read ();
    }
    return rgb | ((hasAlpha ? read () : 0xff) << 24);
  }
  
  public int available () {
    return data.length - pos;
  }  
}
