/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.jpeg;

import java.io.IOException;
import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import info.joriki.io.Writeable;
import info.joriki.io.WriteableByteArray;
import info.joriki.io.WriteableOutputStream;
import info.joriki.io.NybbleInputStream;
import info.joriki.io.NybbleOutputStream;

import info.joriki.io.filter.BitBuffer;
import info.joriki.io.filter.InputFilter;
import info.joriki.io.filter.OutputFilter;
import info.joriki.io.filter.Concatenator;
import info.joriki.io.filter.Crank;
import info.joriki.io.filter.BytesToBitsConverter;
import info.joriki.io.filter.BitsToBytesConverter;

import info.joriki.awt.image.ImageProperties;
import info.joriki.compression.huffman.HuffmanCode;

public class JPEGWriter extends JPEGScribe implements JPEGOptions
{
  MCULayout layout;
  JPEGFormat format;

  Writeable [] [] huffmanSinks = new Writeable [4] [2];

  int width,height;
  int mcuWidth,mcuHeight;

  OutputStream out;
  WriteableOutputStream wos;
  ByteArrayOutputStream baos;
  DataOutputStream dos;
  
  BitBuffer bitBuffer;
  OutputFilter bitsToBytes;

  WriteableByteArray bitArray;
  WriteableByteArray byteArray;
  WriteableByteArray indexArray;
  NybbleOutputStream indexStream;

  int restartInterval;

  byte [] iccData;

  public JPEGWriter ()
  {
    this (0);
  }

  public JPEGWriter (int restartInterval)
  {
    this.restartInterval = restartInterval;
  }

  public void setSink (Object sink)
  {
    this.out = (OutputStream) sink;
    this.wos = new WriteableOutputStream () {
        public void write (int b) throws IOException
        {
          out.write (b);
          if (b == ESC)
            out.write (0);
        }};
  }

  void markSegment (int type) throws IOException
  {
    out.write (ESC);
    out.write (type);
  }

  void startSegment (int type) throws IOException
  {
    markSegment (type);
    baos = new ByteArrayOutputStream ();
    dos = new DataOutputStream (baos);
  }

  void endSegment () throws IOException
  {
    int size = baos.size () + 2;
    out.write (size >> 8);
    out.write (size);
    baos.writeTo (out);
  }

  public void setFormat (JPEGFormat format)
  {
    this.format = format;
    this.layout = format.layout;
  }

  class StatisticsGatherer extends WriteableOutputStream
  {
    int index;
    int [] count = new int [256];

    StatisticsGatherer (int index)
    {
      this.index = index;
    }

    public void write (int b) throws IOException
    {
      b &= 0xff;
      byteArray.write (b);
      indexStream.write (index);
      count [b]++;
    }
    
    HuffmanCode getHuffmanCode ()
    {
      return new HuffmanCode (null,count,16);
    }
  }

  void setupBits ()
  {
    bitBuffer = new BitBuffer (false,true); // flush with |s
    bitsToBytes = Concatenator.concatenate
      (bitBuffer,new BitsToBytesConverter ());
  }

  void setupCodes ()
  {
    bitsToBytes.setSink (wos);
    layout.tableSpec.setSink (bitsToBytes);

    for (int i = 0;i < 4;i++) // code number
      for (int j = 0;j < 2;j++) // ACDC
        huffmanSinks [i] [j] = layout.tableSpec.getHuffmanSink (i,j);
  }

  boolean initialized = false;

  void initialize () throws IOException
  {
    setupBits ();

    layout.restartOutput ();
    layout.defaultCodeBytes ();

    if (defaultHuffmanCodes.isSet ())
    {
      layout.tableSpec.defaultCodes ();
      setupCodes ();
      writePreface ();
    }
    else
    {
      bitArray = new WriteableByteArray ();
      byteArray = new WriteableByteArray ();
      indexArray = new WriteableByteArray ();
      indexStream = new NybbleOutputStream (indexArray);
      bitsToBytes.setSink (bitArray);
      
      for (int i = 0;i < (layout.mono ? 1 : 2);i++) // code number
        for (int j = 0;j < 2;j++) // ACDC
          huffmanSinks [i] [j] = new StatisticsGatherer ((i << 1) + j);
    }

    layout.sinkTo (this);
    initialized = true;
  }

  void writePreface () throws IOException
  {
    markSegment (SOI); // start of input
    
    // Batik expects APP0 marker in embedded JPEGs
    startSegment (APP0);
    baos.write ("JFIF".getBytes ());
    baos.write (0); // string termination
    baos.write (1); // major version
    baos.write (2); // minor version
    baos.write (0); // units: none
    dos.writeShort (1); // x aspect
    dos.writeShort (1); // y aspect
    baos.write (0); // x thumb size
    baos.write (0); // y thumb size
    endSegment ();

 /* We used to only write an APP14 marker if the color transform
    wasn't the PDF default (1 for 3 components, 0 otherwise).
    This caused a problem on page 11 of newvideo2007iss9-ae.pdf,
    which contains two CMYK images that contain a JFIF marker
    and an Adobe marker with color transform 0. If these are
    rewritten without the Adobe marker (0 being the default
    for 4 components), Adobe Reader 8 displays them correctly
    but Acrobate Reader 5.0 transforms the colors. Thus, we
    now always explicitly include an APP14 marker. Note that
    http://java.sun.com/j2se/1.4.2/docs/api/javax/imageio/metadata/doc-files/jpeg_metadata.html
    says that "the usual JPEG conventions" specify that "If
    a JFIF APP0 marker segment is present, the colorspace is
    known to be either grayscale or YCbCr", whereas a value
    of 0 in the Adobe APP14 marker segment is interpreted as
    "Unknown [...] 4-channel images are assumed to be CMYK".
    So according to these "conventions" the original file
    contained conflicting information, and removing the APP14
    marker caused it to be interpreted as YCbCr -- so it might
    be that Acrobat Reader 5.0 follows these "conventions"
    whereas Adobe Reader 8 implements the PDF spec. */

    startSegment (APP14);
    baos.write ("Adobe".getBytes ());
    dos.writeShort (100); // version
    dos.writeShort (0);   // flags0    don't know what these mean
    dos.writeShort (0);   // flags1
    baos.write (layout.colorTransform);
    endSegment ();
    
    // write comment
    String comment = (String) format.get (ImageProperties.COMMENT);
    if (comment != null)
    {
      startSegment (COM);
      baos.write (comment.getBytes ());
      endSegment ();
    }

    // write ICC data
    if (iccData != null)
    {
      startSegment (APP2);
      baos.write ("ICC_PROFILE".getBytes ());
      baos.write (0); // string termination
      baos.write (1); // index of block
      baos.write (1); // count of blocks
      baos.write (iccData);
      endSegment ();
    }
    
    // write huffman codes
    startSegment (DHT);
    for (int i = 0;i < 4;i++)
      for (int j = 0;j < 2;j++)
        {
          HuffmanCode code = layout.tableSpec.huffmanCodes [i] [j];
          if (code != null)
            {
              baos.write (i | (j << 4));
              code.writeTo (baos);
            }
        }
    endSegment ();

    // write quantization tables
    startSegment (DQT);
    for (int i = 0;i < 4;i++)
      {
        QuantizationTable table = layout.tableSpec.quantizationTables [i];
        if (table != null)
          {
            boolean twoByte = table.isTwoByte ();
            // CHG 25/03/02
            baos.write (twoByte ? (i | 0x10) : i);
            table.writeTo (dos,twoByte);
          }
      }
    endSegment ();

    // write restart interval
    if (restartInterval != 0)
      {
        startSegment (DRI);
        dos.writeShort (restartInterval);
        endSegment ();
      }

    // write image format
    startSegment (SOF0);
    baos.write (8);
    dos.writeShort (format.pixelHeight);
    dos.writeShort (format.pixelWidth);
    baos.write (layout.components.length);
    for (int i = 0;i < layout.components.length;i++)
      {
        JPEGComponent component = layout.components [i];
        baos.write (component.id);
        baos.write ((component.hSamp << 4) | component.vSamp);
        baos.write (component.quant);
      }
    endSegment ();

    // start of scan
    startSegment (SOS);
    layout.writeTo (baos);
    endSegment ();
  }

  JPEGRequest request = new JPEGRequest (RAW);
  int mcuCount = 0;
  int restartCount = 0;

  void restart () throws IOException
  {
    bitBuffer.flush ();
    out.write (ESC);
    out.write (RST0 + restartCount);
    if (++restartCount == 8)
      restartCount = 0;
  }

  void secondPass () throws IOException
  {
    bitBuffer.flush ();
    BitBuffer bits = new BitBuffer (false,true);
    InputFilter bytesToBits = Concatenator.concatenate (new BytesToBitsConverter (),bits);
    bytesToBits.setSource (new ByteArrayInputStream (bitArray.toByteArray ()));

    layout.tableSpec.huffmanCodes = new HuffmanCode [4] [2];
    for (int i = 0;i < (layout.mono ? 1 : 2);i++) // code number
      for (int j = 0;j < 2;j++) // ACDC
        layout.tableSpec.huffmanCodes [i] [j] =
          ((StatisticsGatherer) huffmanSinks [i] [j]).getHuffmanCode ();
    setupBits ();
    setupCodes ();
    writePreface ();
    
    byte [] symbols = byteArray.toByteArray ();
    indexStream.flush ();
    NybbleInputStream indices = new NybbleInputStream (new ByteArrayInputStream (indexArray.toByteArray ()));

    for (int i = 0;i < symbols.length;i++)
      {
        int index = indices.read ();
        if (index == 0xf)
          restart ();
        else
          {
            byte symbol = symbols [i];
            huffmanSinks [index >> 1] [index & 1].write (symbol);
            int size = symbol & 0xf;
            bitBuffer.writeBits (bits.readBits (size),size);
          }
      }
  }

  public int crank () throws IOException
  {
    if (request.y == format.height)
      return Crank.EOI;

    if (!initialized)
      initialize ();

    int eod = source.readRequest (request);
    if (eod != OK)
      {
        if (eod == Crank.EOI)
          bitBuffer.flush ();
        return eod;
      }
    layout.writeRawData (format.mcus [request.y] [request.x]);
    if (++mcuCount == restartInterval)
      {
        if (defaultHuffmanCodes.isSet ())
          restart ();
        else
        {
          indexStream.write (0xf);
          byteArray.write (0); // dummy value
        }

        layout.restartOutput ();
        mcuCount = 0;
      }

    source.deallocationRequest (request);

    do
      if (++request.x == format.width)
        {
          request.x = 0;
          if (++request.y == format.height)
            {
              if (!defaultHuffmanCodes.isSet ())
                secondPass ();
              bitBuffer.flush ();
              markSegment (JPEGSpeaker.EOI);
              out.flush ();
              out.close ();
              return OK;
            }
        }
    // TODO: It seems that this is now handled in JPEGUpSampler? 
    // this is currently only for JPEGUpSampler, so we don't have
    // to worry about discarding empty MCUs at the borders. It
    // might be better to the latter, but it would add a lot of
    // special cases to JPEGUpSampler
    while (request.x * format.layout.width  >= format.pixelWidth ||
           request.y * format.layout.height >= format.pixelHeight);

    return OK;
  }

  public void setICCData (byte [] iccData) {
    this.iccData = iccData;
  }
}
