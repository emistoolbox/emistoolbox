/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.jpeg;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.awt.Dimension;

import java.awt.image.ColorModel;

import java.util.Stack;
import java.util.Arrays;

import info.joriki.io.Readable;
import info.joriki.io.Writeable;
import info.joriki.io.BitSource;
import info.joriki.io.BitSink;

import info.joriki.awt.image.YUVColorModel;
import info.joriki.awt.image.CartesianTransform;

import info.joriki.util.DebugStack;
import info.joriki.util.Assertions;
import info.joriki.util.NotImplementedException;
import info.joriki.util.NotTestedException;
import info.joriki.util.Options;

public class MCULayout implements JPEGSpeaker
{
  final static boolean debugging = false;

  Stack shortStack = debugging ? new DebugStack ("short") : new Stack ();
  Stack floatStack = debugging ? new DebugStack ("float") : new Stack ();
  Stack bytesStack = debugging ? new DebugStack ("bytes") : new Stack ();
  Stack pixelStack = debugging ? new DebugStack ("pixel") : new Stack ();

  JPEGComponent [] components;
  int [] indices;
  byte [] codeByte;
  Readable [] [] huffmanSources;
  Writeable [] [] huffmanSinks;

  TableSpecification tableSpec;

  int width,height;
  int blockWidth,blockHeight;

  BitSource bitSource;
  BitSink bitSink;

  boolean mono;
  int colorTransform;
  boolean invertCMYK;
  /*
    CMYK inversion used to be handled here.
    That doesn't work, though, since PDF files
    explicitly do the inversion using the decode array.
    So we need to leave the values as they are and
    let every client perform the inversion itself.
    We carry the information along in the layout.
  */

  boolean refinement;

  int scanStart;
  int scanLimit;

  void init ()
  {
    int ncomponent = components.length;
    Assertions.limit (ncomponent,1,4);
    codeByte = new byte [ncomponent];
    huffmanSources = new Readable [ncomponent] [2];
    huffmanSinks = new Writeable [ncomponent] [2];
    mono = ncomponent == 1;
  }

  MCULayout (ColorModel colorModel,Dimension chromaticitySampling)
  {
    this (colorModel.getNumColorComponents (),
          chromaticitySampling.width,
          chromaticitySampling.height,
          new TableSpecification ());
  }

  MCULayout (MCULayout layout,int h,int v)
  {
    this (layout.components.length,h,v,layout.tableSpec);
  }

  MCULayout (int ncomponent,int h,int v,TableSpecification tableSpec)
  {
    this (ncomponent == 1 ?
      new JPEGComponent [] {
        new JPEGComponent (1,1,1,0)
          } :
      ncomponent == 3 ?
        new JPEGComponent [] {
          new JPEGComponent (1,h,v,0),
          new JPEGComponent (2,1,1,1),
          new JPEGComponent (3,1,1,1)
            } :
      new JPEGComponent [] {
        new JPEGComponent (1,h,v,0),
        new JPEGComponent (2,1,1,1),
        new JPEGComponent (3,1,1,1),
        new JPEGComponent (4,h,v,0)
          },
          tableSpec,ncomponent >> 1);
  }
  
  MCULayout (JPEGComponent [] components,TableSpecification tableSpec,int colorTransform) {
    this.components = components;
    this.tableSpec = tableSpec;
    this.colorTransform = colorTransform;
    init ();
    calculateDimensions ();
    defaultCodeBytes ();
  }

  MCULayout (MCULayout layout,String quantizationTables) throws IOException {
    this (layout.components,new TableSpecification (quantizationTables),layout.colorTransform);
  }

  MCULayout (int ncomponent,int colorTransform,boolean invertCMYK)
  {
    components = new JPEGComponent [ncomponent];
    this.colorTransform = colorTransform;
    this.invertCMYK = invertCMYK;
    init ();
  }

  MCULayout (MCULayout layout)
  {
    this (layout.components.length,layout.colorTransform,layout.invertCMYK);
    System.arraycopy (layout.codeByte,0,codeByte,0,codeByte.length);
  }
  
  MCULayout (JPEGReader dad,int colorTransform,boolean invertCMYK)
  {
    components = dad.components;
    bitSource = dad.bitSource;
    tableSpec = dad.tableSpec;
    this.colorTransform = colorTransform;
    this.invertCMYK = invertCMYK;
    init ();
    calculateDimensions ();
  }

  // The return value is non-null for AC scans with non-trivial
  // MCU layout, i.e. for AC scans of components with hsamp != 1
  // or vsamp != 1. In this case the component being scanned is returned.
  // Ghostview claims that this is always one at a time, and both
  // Ghostview and experience show that in this case the data is in
  // sequentially ordered 8x8 blocks, not in the MCU order of full
  // and DC scans. For hsamp = vsamp = 1 the two are the same.
  // Note that while for vsamp = 1 the order is the same, there
  // may be spurious blocks in the right margin for hsamp != 1.
  JPEGComponent startScan (JPEGReader dad,InputStream in,boolean buggyIDs)
    throws IOException
  {
    indices = new int [in.read ()];
    
    for (int i = 0;i < indices.length;i++)
      {
        int id = in.read ();
        if (buggyIDs && i == 3)
          {
            Assertions.expect (id,1);
            id = 4;
          }
        indices [i] = ((Integer) dad.indices.get (new Integer (id))).intValue ();
        Assertions.unexpect (components [indices [i]],null);

        codeByte [i] = (byte) in.read ();
        for (int acdc = 0;acdc < 2;acdc++)
          {
            int code = (codeByte [i] >> ((1 - acdc) << 2)) & 0xf;
            huffmanSources [i] [acdc] = tableSpec.getHuffmanSource (code,acdc);
          }
      }

    // only spectral selection is currently implemented
    // for progressive mode
    scanStart = in.read ();
    scanLimit = in.read () + 1;
    Assertions.limit (scanStart,0,scanLimit);
    Assertions.limit (scanLimit,scanStart,DCTsize);
    // It seems that in progressive mode DC is always separate.
    boolean fullScan = scanStart == 0 && scanLimit == DCTsize;
    boolean dcScan = scanStart == 0 && scanLimit == 1;
    boolean acScan = scanStart > 0;
    Assertions.expect (fullScan || dcScan || acScan);
    int bits = in.read ();
    int hiBit = (bits >> 4) & 0xf;
    int loBit = (bits >> 0) & 0xf;

    refinement = hiBit != 0;
    if (refinement)
      Assertions.expect (loBit,hiBit - 1);

    if (loBit == 0) // last scan for this spectral range
      for (int i = 0;i < indices.length;i++)
        components [indices [i]].mark (scanStart,scanLimit);

    if (acScan)
      {
        Assertions.expect (indices.length,1); 
        JPEGComponent component = components [indices [0]];
        if (component.hSamp != 1 || component.vSamp != 1)
          return component;
      }
    return null;
  }

  boolean isComplete ()
  {
    for (int i = 0;i < components.length;i++)
      if (!components [i].isComplete ())
        return false;
    return true;
  }

  void sinkTo (JPEGWriter writer)
  {
    bitSink = writer.bitBuffer;

    for (int i = 0;i < components.length;i++)
      for (int acdc = 0;acdc < 2;acdc++)
        {
          int code = (codeByte [i] >> ((1 - acdc) << 2)) & 0xf;
          huffmanSinks [i] [acdc] = writer.huffmanSinks [code] [acdc];
        }
  }
  
  void writeTo (OutputStream out) throws IOException
  {
    out.write (components.length);
    
    for (int i = 0;i < components.length;i++)
      {
        out.write (components [i].id);
        out.write (codeByte [i]);
      }

    // fancy parameters that are fixed for sequential JPEG (see above)
    out.write (0);
    out.write (63);
    out.write (0);
  }

  public void calculateDimensions ()
  {
    blockHeight = blockWidth = 0;
    for (int i = 0;i < components.length;i++)
      {
        blockHeight = Math.max (blockHeight,components [i].vSamp);
        blockWidth = Math.max (blockWidth,components [i].hSamp);
      }

    height = blockHeight * DCTlength;
    width = blockWidth * DCTlength;
  }

  void allocate (byte type,MCU mcu)
  {
    switch (type)
      {
      case RAW :
        if (mcu.shortData != null)
          break;
        if (shortStack.isEmpty ())
          {
            mcu.shortData = new short [components.length] [] [];
            for (int i = 0;i < components.length;i++)
              mcu.shortData [i] = new short [components [i].size] [DCTsize];
          }
        else
          mcu.shortData = (short [] [] []) shortStack.pop ();
        break;
      case FOURIER :
      case DIRECT :
      case COLORS :
        if (mcu.floatData != null)
          break;
        if (floatStack.isEmpty ())
          {
            mcu.floatData = new float [components.length] [] [];
            for (int i = 0;i < components.length;i++)
              mcu.floatData [i] = new float [components [i].size] [DCTsize];
          }
        else
          mcu.floatData = (float [] [] []) floatStack.pop ();
        break;
      case PIXELS :
        if (mono && mcu.byteData == null)
          mcu.byteData =
            bytesStack.isEmpty () ?
            new byte [width * height] :
              (byte []) bytesStack.pop ();
        else if (!mono && mcu.pixelData == null)
          mcu.pixelData =
            pixelStack.isEmpty () ?
            new int [width * height] :
              (int []) pixelStack.pop ();
        break;
      default :
        throw new InternalError ();
      }
    mcu.type = type;
  }

  void deallocate (byte type,MCU mcu)
  {
    switch (type)
      {
      case RAW :
        shortStack.push (mcu.shortData); mcu.shortData = null; break;
      case FOURIER :
      case DIRECT :
      case COLORS :
        floatStack.push (mcu.floatData); mcu.floatData = null; break;
      case PIXELS :
        if (mono)
          {
            bytesStack.push (mcu.byteData);
            mcu.byteData = null;
          }
        else
          {
            pixelStack.push (mcu.pixelData);
            mcu.pixelData = null;
          }
        break;
      default :
        throw new InternalError ();
      }
  }

  void transformTo (byte min,byte max,MCU mcu)
  {
    Assertions.expect (min <= max);

    if (mcu.type < min) // less than means closer to raw data
      switch (mcu.type)
        {
        case RAW :
          dequantize (mcu);
          if (min == FOURIER)
            break;
        case FOURIER :
          inverseFourierTransform (mcu);
          if (min == DIRECT)
            break;
        case DIRECT :
          colorTransform (mcu);
          if (min == COLORS)
            break;
        case COLORS :
          pixelize (mcu);
        }
    else if (mcu.type > max)
      switch (mcu.type)
        {
        case PIXELS :
          depixelize (mcu);
          if (max == COLORS)
            break;
        case COLORS :
          inverseColorTransform (mcu);
          if (max == DIRECT)
            break;
        case DIRECT :
          fourierTransform (mcu);
          if (max == FOURIER)
            break;
        case FOURIER :
          quantize (mcu);
        }
  }

  final static private float clipColor (float color)
  {
    return color < 0 ? 0 : color > 255 ? 255 : color;
  }
  
  private void clip (float [] arr)
  {
    Assertions.expect (arr.length,DCTsize);
    for (int j = 0;j < DCTsize;j++)
      arr [j] = clipColor (arr [j]);
  }

  private void shift (float [] arr,float shift)
  {
    Assertions.expect (arr.length,DCTsize);
    for (int j = 0;j < DCTsize;j++)
      arr [j] += shift;
  }

  private void invert (MCU mcu)
  {
    for (int i = 0;i < 3;i++)
      for (int j = 0;j < components [i].size;j++)
        invert (mcu.floatData [i] [j]);
  }

  private void invert (float [] arr)
  {
    Assertions.expect (arr.length,DCTsize);
    for (int k = 0;k < DCTsize;k++)
      arr [k] = 255 - arr [k];
  }

  private void shift (MCU mcu,int shift) {
    for (int i = 0;i < components.length;i++)
      if (colorTransform == 0 || i == 0 || i == 3) // if transformed, shift only Y and K
        shift (mcu.floatData [i] [0],shift);
  }
  
  private void checkAndInvert (MCU mcu) {
    Assertions.expect (blockWidth,1);
    Assertions.expect (blockHeight,1);
    switch (colorTransform)
    {
    case 0 : break;
    case 1 : Assertions.expect (components.length,3); break;
    case 2 : Assertions.expect (components.length,4); invert (mcu); break;
    default: throw new NotImplementedException ("color transform " + colorTransform); 
    }
  }

  void colorTransform (MCU mcu)
  {
    Assertions.expect (mcu.type,DIRECT);

    shift (mcu,128);

    if (colorTransform != 0)
    {
      Assertions.limit (components.length,3,4);
      Assertions.expect (colorTransform,components.length - 2);

      float [] ys = mcu.floatData [0] [0];
      float [] us = mcu.floatData [1] [0];
      float [] vs = mcu.floatData [2] [0];

      for (int j = 0;j < DCTsize;j++)
      {
        float y = ys [j];
        float u = us [j];
        float v = vs [j];

        ys [j] = y                        + YUVColorModel.VR * v;
        us [j] = y + YUVColorModel.UG * u + YUVColorModel.VG * v;
        vs [j] = y + YUVColorModel.UB * u                       ;
      }
    }

    for (int i = 0;i < components.length;i++)
      clip (mcu.floatData [i] [0]);
    
    checkAndInvert (mcu);
    
    mcu.type = COLORS;
  }
  
  final float [] rgb = new float [3];
  final float [] yuv = new float [3];

  void inverseColorTransform (MCU mcu)
  {
    if (components.length == 2)
      throw new NotTestedException ();
    
    Assertions.expect (mcu.type,COLORS);

    checkAndInvert (mcu);
    
    if (colorTransform != 0)
    {  
      float [] ys = mcu.floatData [0] [0];
      float [] us = mcu.floatData [1] [0];
      float [] vs = mcu.floatData [2] [0];

      for (int j = 0;j < DCTsize;j++)
      {
        rgb [0] = ys [j];
        rgb [1] = us [j];
        rgb [2] = vs [j];

        for (int i = 0;i < 3;i++)
        {
          float sum = 0;
          for (int m = 0;m < 3;m++)
            sum += YUVColorModel.RGBtoYUV [i] [m] * rgb [m];
          yuv [i] = sum;
        }
        
        ys [j] = yuv [0];
        us [j] = yuv [1];
        vs [j] = yuv [2];
      }
    }

    shift (mcu,-128);
      
    mcu.type = DIRECT;
  }

  void depixelize (MCU mcu)
  {
    Assertions.expect (mcu.type,PIXELS);
    Assertions.expect (blockWidth,1);
    Assertions.expect (blockHeight,1);

    allocate (COLORS,mcu);
    for (int y = 0,j = 0;y < DCTlength;y++)
      for (int x = 0;x < DCTlength;x++,j++)
        if (mono)
          mcu.floatData [0] [0] [j] = mcu.byteData [j] & 0xff;
        else
          {
            int pixel = mcu.pixelData [j];
            for (int i = components.length - 1;i >= 0;i--)
              {
                mcu.floatData [i] [0] [j] = pixel & 0xff;
                pixel >>= 8;
              }
          }
    deallocate (PIXELS,mcu);
  }

  void pixelize (MCU mcu)
  {
    Assertions.expect (mcu.type,COLORS);
    Assertions.expect (blockWidth,1);
    Assertions.expect (blockHeight,1);

    allocate (PIXELS,mcu);
    for (int y = 0,j = 0;y < DCTlength;y++)
      for (int x = 0;x < DCTlength;x++,j++)
        if (mono)
          mcu.byteData [j] = (byte) Math.round (clipColor (mcu.floatData [0] [0] [j]));
        else
          {
            int pixel = 0;
            for (int i = 0;i < components.length;i++)
              {
                pixel <<= 8;
                pixel |= Math.round (clipColor (mcu.floatData [i] [0] [j]));
              }
            mcu.pixelData [j] = pixel;
          }
    deallocate (COLORS,mcu);
  }

  void inverseFourierTransform (MCU mcu)
  {
    Assertions.expect (mcu.type,FOURIER);
    for (int i = 0;i < components.length;i++)
      for (int j = 0;j < components [i].size;j++)
        JPEGCosineTransform.inverseTransform (mcu.floatData [i] [j]);
    mcu.type = DIRECT;
  }

  void fourierTransform (MCU mcu)
  {
    Assertions.expect (mcu.type,DIRECT);
    for (int i = 0;i < components.length;i++)
      for (int j = 0;j < components [i].size;j++)
        JPEGCosineTransform.transform (mcu.floatData [i] [j]);
    mcu.type = FOURIER;
  }

  void quantize (MCU mcu)
  {
    Assertions.expect (mcu.type,FOURIER);
    allocate (RAW,mcu);
    for (int i = 0;i < components.length;i++)
      for (int j = 0;j < components [i].size;j++)
        tableSpec.quantizationTables [components [i].quant].quantize (mcu.floatData [i] [j],mcu.shortData [i] [j]);
    deallocate (FOURIER,mcu);
  }

  void dequantize (MCU mcu)
  {
    Assertions.expect (mcu.type,RAW);
    allocate (FOURIER,mcu);
    for (int i = 0;i < components.length;i++)
      for (int j = 0;j < components [i].size;j++)
        tableSpec.quantizationTables [components [i].quant].dequantize (mcu.shortData [i] [j],mcu.floatData [i] [j]);
    deallocate (RAW,mcu);
  }

  boolean err = false;

  void readRawData (MCU mcu) throws IOException
  {
    Assertions.expect (mcu.type,RAW);
    if (!err)
      try {
        for (int i = 0;i < indices.length;i++)
          for (int j = 0;j < components [indices [i]].size;j++)
            huffmanDecode (mcu,i,j);
      } catch (IllegalArgumentException iae) {
        err = true;
      }
  }

  void huffmanDecode (MCU mcu,int i,int j) throws IOException
  {
    int index = indices [i];
    huffmanDecode (huffmanSources [i],mcu.shortData [index] [j],components [index]);
  }

  void writeRawData (MCU mcu) throws IOException
  {
    Assertions.expect (mcu.type,RAW);
    for (int i = 0;i < components.length;i++)
      for (int j = 0;j < components [i].size;j++)
        huffmanEncode (huffmanSinks [i],mcu.shortData [i] [j],components [i]);
  }

  int nEOB = 0;

  void huffmanDecode (Readable [] sources,short [] dest,JPEGComponent component) throws IOException
  {
    int k = scanStart;

    if (nEOB == 0)
      {
        if (refinement)
          {
            if (scanStart == 0) // DC scan
              {
                dest [0] <<= 1;
                dest [0] |= bitSource.readBits (1);
              }
            else // AC scan
              {
                Readable source = sources [AC];

                for (;k < scanLimit;k++)
                  {
                    int code = source.read ();
                    int size = code & 0xf;
                    int run = code >> 4;
                    short sign;
    
                    if (size != 0)
                      {
                        Assertions.expect (size,1);
                        sign = bitSource.readBits (1) == 1 ? (short) 1 : (short) -1;
                      }
                    else if (run != 15)
                      {
                        nEOB = (1 << run) | bitSource.readBits (run);
                        break;
                      }
                    else
                      sign = 0;
    
                    // a work of art :-)
                    while (!(append (dest,k) && --run < 0) && ++k < scanLimit)
                      ;
    
                    if (sign != 0)
                      dest [ZigZag.zigZag [k]] = sign;
                  }
              }
          }
        else
          {
            Readable source = sources [scanStart == 0 ? DC : AC];
    
            for (;k < scanLimit;k++)
              {
                int code = source.read ();
                int size = code & 0xf;
                int run = code >> 4;

                if (size == 0)
                  {
                    if (k != 0)
                      {
                        if (run != 15)
                          {
                            // in baseline mode, this reduces to nEOB = 1
                            // its meaning in progressive mode is elucidated
                            // in "jpeg in japanese", for some values of
                            // "elucidated". Ghostview does the same thing.
                            // 06/Oct/02: This is also explained in ITU T81
                            nEOB = (1 << run) | bitSource.readBits (run);
                            break;
                          }
                        k += 15; // and one in loop increment == 16
                      }
                  }
                else
                  {
                    k += run;
                    short bits = (short) bitSource.readBits (size);
                    if (bits < (1 << (size - 1)))
                      bits -= (1 << size) - 1;
                    try {
                      dest [ZigZag.zigZag [k]] = bits;
                    } catch (ArrayIndexOutOfBoundsException aioobe) {
                      Options.warn ("invalid run length in JPEG data");
                    }
                  }
                source = sources [AC];
              }
          }
      }

    if (nEOB > 0)
      {
        if (refinement)
          for (;k < scanLimit;k++)
            append (dest,k);
        nEOB--;
      }

    if (scanStart == 0 && !refinement)
      dest [0] = component.lastReadDC += dest [0];
  }

  boolean append (short [] dest,int k) throws IOException
  {
    int index = ZigZag.zigZag [k];
    short value = dest [index];
    if (value == 0)
      return true;

    value <<= 1;
    if (bitSource.readBits (1) == 1)
      {
        if (value > 0)
          value++;
        else
          value--;
      }
    dest [index] = value;
    return false;
  }

  final static int maxSize = 11;
  final static byte [] sizeLookup = new byte [(1 << (maxSize + 1)) - 1];
  final static int sizeOffset = (1 << maxSize) - 1;

  static {
    for (int i = 1;i <= maxSize;i++)
      for (int j = (1 << (i - 1));j < (1 << i);j++)
        sizeLookup [sizeOffset + j] = sizeLookup [sizeOffset - j] = (byte) i;
  }

  void huffmanEncode (Writeable [] sinks,short [] src,JPEGComponent component) throws IOException
  {
    short tmpDC = src [0];
    src [0] -= component.lastWriteDC;

    Writeable sink = sinks [DC];

    for (int k = 0;k < DCTsize;k++)
      {
        int run = 0;
        if (k != 0)
          {
            while (k < DCTsize && src [ZigZag.zigZag [k]] == 0)
              {
                run++;
                k++;
              }
            if (k == DCTsize)
              {
                sink.write (0);
                break;
              }
            while (run > 15)
              {
                sink.write (0xf0);
                run -= 16;
              }
          }

        int d = src [ZigZag.zigZag [k]];

        int size;
        try {
          size = sizeLookup [d + sizeOffset];
        } catch (ArrayIndexOutOfBoundsException aioobe) {
          System.out.println (d + " " + sizeOffset + " ! " + k);
          throw aioobe;
        }
        try {
          sink.write ((run << 4) | size);
        } catch (NullPointerException npe) {
          System.out.println (k + " " + run + " " + size);
          throw npe;
        }
        bitSink.writeBits (d < 0 ? d + (1 << size) - 1 : d,size);
        sink = sinks [AC];
      }

    component.lastWriteDC = src [0] = tmpDC;
  }

  public void restartInput ()
  {
    for (int i = 0;i < indices.length;i++)
      components [indices [i]].lastReadDC = 0;

    Assertions.expect (nEOB,0);
  }

  public void restartOutput ()
  {
    for (int i = 0;i < components.length;i++)
      components [i].lastWriteDC = 0;
  }

  public String toString ()
  {
    StringBuilder stringBuilder = new StringBuilder ();

    stringBuilder.append (super.toString ()).append ("\n");
    stringBuilder.append (components.length).append (" components\n");
    stringBuilder.append (blockWidth).append ('x').append (blockHeight).append ('\n');
    for (int i = 0;i < components.length;i++)
      stringBuilder.append (i).append (" : ").append (components [i].hSamp)
        .append ('x').append (components [i].vSamp).append ('\n');
    return stringBuilder.toString ();
  }

  public void copy (MCU src,MCU dest)
  {
    switch (dest.type = src.type)
      {
      case RAW :
        for (int i = 0;i < components.length;i++)
          for (int j = 0;j < components [i].size;j++)
            System.arraycopy
              (src.shortData [i] [j],0,dest.shortData [i] [j],0,DCTsize);
        break;
      case FOURIER :
      case DIRECT :
      case COLORS :
        for (int i = 0;i < components.length;i++)
          for (int j = 0;j < components [i].size;j++)
            System.arraycopy
              (src.floatData [i] [j],0,dest.floatData [i] [j],0,DCTsize);
        break;
      case PIXELS :
        if (mono)
          System.arraycopy
            (src.byteData,0,dest.byteData,0,src.byteData.length);
        else
          System.arraycopy
            (src.pixelData,0,dest.pixelData,0,src.pixelData.length);
        break;
      default :
        throw new InternalError ();
      }
  }

  public MCULayout transformedBy (CartesianTransform transform)
  {
    MCULayout transformed = new MCULayout (this);

    for (int i = 0;i < components.length;i++)
      transformed.components [i] = components [i].transformedBy (transform);

    transformed.tableSpec = tableSpec.transformedBy (transform);

    if (transform.swaps ())
      {
        transformed.width = height;
        transformed.height = width;
        transformed.blockWidth = blockHeight;
        transformed.blockHeight = blockWidth;
      }
    else
      {
        transformed.width = width;
        transformed.height = height;
        transformed.blockWidth = blockWidth;
        transformed.blockHeight = blockHeight;
      }

    return transformed;
  }

  // dest has this layout, src may be different.
  public void makeTransformedCopy (MCU src,MCU dest,CartesianTransform transform)
  {
    Assertions.expect (src.type,RAW);

    boolean swaps = transform.swaps ();
    boolean invertsX = transform.invertsX ();
    boolean invertsY = transform.invertsY ();
    int signX = invertsX ? -1 : 1;
    int signY = invertsY ? -1 : 1;

    for (int i = 0;i < components.length;i++)
      {
        JPEGComponent component = components [i];
        int vSamp = component.vSamp;
        int hSamp = component.hSamp;
        int srchSamp = swaps ? vSamp : hSamp;
        for (int dy = 0;dy < vSamp;dy++)
          for (int dx = 0;dx < hSamp;dx++)
            {
              // traversing transform parts in reverse order to get from
              // dest to src
              int cx = invertsX ? hSamp - 1 - dx : dx;
              int cy = invertsY ? vSamp - 1 - dy : dy;
              if (swaps)
                {
                  int tmp = cx;
                  cx = cy;
                  cy = tmp;
                }
              short [] srcData = src.shortData [i] [cy * srchSamp + cx];
              short [] destData = dest.shortData [i] [dy * hSamp + dx];
              for (int y = 0,ysign = 1,index = 0;
                   y < DCTlength;
                   y++,ysign *= signY)
                for (int x = 0,xsign = ysign;
                     x < DCTlength;
                     x++,index++,xsign *= signX)
                  destData [index] =
                    (short) (xsign * srcData [swaps ? x * DCTlength + y : index]);
            }
      }
  }

  public void clear (MCU mcu)
  {
    if (mcu.type == PIXELS)
      {
        if (mono)
          Arrays.fill (mcu.byteData,(byte) 0);
        else
          Arrays.fill (mcu.pixelData,0);
      }
    else
      for (int i = 0;i < components.length;i++)
        for (int j = 0;j < components [i].size;j++)
          switch (mcu.type)
            {
            case RAW :
              Arrays.fill (mcu.shortData [i] [j],(short) 0);
              break;
            case FOURIER :
            case DIRECT :
            case COLORS :
              Arrays.fill (mcu.floatData [i] [j],0);
              break;
            default :
              throw new InternalError ();
            }
  }

  public void defaultCodeBytes ()
  {
    for (int i = 0;i < components.length;i++)
      codeByte [i] = (byte) (i == 0 || i == 3 ? 0x00 : 0x11);
  }

  public boolean equals (Object o)
  {
    if (!(o instanceof MCULayout))
      return false;
    MCULayout layout = (MCULayout) o;
    if (layout.components.length != components.length ||
        layout.blockWidth != blockWidth ||
        layout.blockHeight != blockHeight)
      return false;

    for (int i = 0;i < components.length;i++)
      if (components [i].hSamp != layout.components [i].hSamp ||
          components [i].vSamp != layout.components [i].vSamp)
        return false;
    return true;
  }

  public void setLossiness (float lossiness) throws IOException
  {
    tableSpec.quantizationTables (lossiness);
  }
  
  public void setQuantizationTables (String name) throws IOException {
    tableSpec.readQuantizationTables (name);
  }

  public boolean hasStandardColorTransform ()
  {
    return colorTransform == components.length >> 1;
  }
  
  public boolean hasDefaultColorTransform ()
  {
    return colorTransform == (components.length == 3 ? 1 : 0);
  }

  public Dimension getChromaticitySampling () {
    return new Dimension (blockWidth,blockHeight);
  }
}

