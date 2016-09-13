/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.jpeg;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataInputStream;

import java.awt.Dimension;

import java.awt.image.ColorModel;

import java.util.Map;
import java.util.HashMap;

import info.joriki.awt.image.DummyColorModel;
import info.joriki.awt.image.ICCColorModel;
import info.joriki.awt.image.RGBColorModel;
import info.joriki.awt.image.GrayColorModel;
import info.joriki.awt.image.CMYKColorModel;
import info.joriki.awt.image.ImageProperties;

import info.joriki.io.Util;
import info.joriki.io.BitSource;
import info.joriki.io.ReadableInputStream;
import info.joriki.io.LimitedInputStream;

import info.joriki.io.filter.BitBuffer;
import info.joriki.io.filter.InputFilter;
import info.joriki.io.filter.Concatenator;
import info.joriki.io.filter.BytesToBitsConverter;

import info.joriki.util.Options;
import info.joriki.util.Assertions;
import info.joriki.util.NotTestedException;
import info.joriki.util.NotImplementedException;

public class JPEGReader extends JPEGInitial
{
  final static int PAD = 0;

  public int colorTransform;
  public MCULayout layout;
  public int width,height;

  JPEGFormat format;
  String comment;
  
  int mcuWidth,mcuHeight;

  InputStream in;
  DataInputStream dis;

  TableSpecification tableSpec = new TableSpecification ();
  JPEGComponent [] components;
  Map indices = new HashMap ();

  BitBuffer bitBuffer = new BitBuffer (false,true);
  InputFilter bytesToBits = Concatenator.concatenate
    (new BytesToBitsConverter (),bitBuffer);
  BitSource bitSource = (BitSource) bytesToBits.getSource ();
  InputStream cookedInput = new ReadableInputStream () {
      public int read () throws IOException
      {
        if (endOfScan)
          return -1;
        if (waitingForRestart)
          return PAD; // This is what jdhuff.c in the Ghostscript
        // source does -- page 18 of brochure22.pdf contains an
        // image that actually relies on this and failed when
        // I returned 0xff here; as far as I can see, this data
        // should never have been used.
        int b = in.read ();
        if (b != ESC)
          return b;
        b = in.read ();
        if (b == 0)
          return ESC;
        else if (RST0 <= b && b <= RST7)
          {
            handleRestartMarker (b);
            waitingForRestart = true;
            return PAD;
          }
        else
          {
            cachedMarker = b;
            endOfScan = true;
            return -1;
          }
      }
    };
  
  int restartInterval = -1;
  boolean endOfScan = false;
  int mode;
    
  boolean invertCMYK;
  boolean afterSOF;

  byte [] [] iccData;

  public JPEGFormat getFormat ()
  {
    return format;
  }

  public JPEGReader (InputStream in) throws IOException
  {
    this (in,1);
  }
  
  public JPEGReader (InputStream in,int externalColorTransform) throws IOException
  {
    this.in = in;
    this.colorTransform = externalColorTransform;

    dis = new DataInputStream (in);
    bytesToBits.setSource (cookedInput);
    tableSpec.setSource (bytesToBits);
    
    Assertions.expect (in.read (),ESC);
    Assertions.expect (in.read (),SOI);

    handleSegments ();
    
    Assertions.expect (afterSOF);

    if (components.length == 4)
      {
        Assertions.unexpect (colorTransform,1);
        boolean letters = true;
        for (int i = 0;i < 4;i++)
          letters &= indices.containsKey (new Integer ("CMYK".charAt (i)));
        invertCMYK |= letters;
      }
    else {
      if (components.length < 3)
        colorTransform = 0;
      invertCMYK = false;
    }

    layout = new MCULayout (this,colorTransform,invertCMYK);
    if (Options.tracing)
      System.out.println (layout);
    mcuHeight = partsOf (layout.height,height);
    mcuWidth = partsOf (layout.width,width);
    format = new JPEGFormat (layout,mcuWidth,mcuHeight,width,height);
    if (comment != null)
      format.put (ImageProperties.COMMENT,comment);
  }

  int cachedMarker = -1;
  boolean buggyIDs = false;

  void handleSegments () throws IOException
  {
    for (;;)
      {
        int segmentType;

        if (cachedMarker != -1)
          {
            segmentType = cachedMarker;
            cachedMarker = -1;
          }
        else
          {
            segmentType = nextMarker ();
	    if (segmentType == -1)
	      {
		Options.warn ("missing EOI marker");
		return;
	      }
          }

        if (Options.tracing)
          System.out.println ("segmentType = " + Integer.toHexString (segmentType));

        if (segmentType == JPEGSpeaker.EOI)
          return;

        if (segmentType == JPEGSpeaker.SOI)
          {
            Options.warn ("SOI marker instead of EOI marker");
            return;
          }

        int length = dis.readUnsignedShort () - 2; // 2 for length itself
        LimitedInputStream lis = new LimitedInputStream (in,length);

        switch (segmentType)
          {
          case DHT : // define huffman table
            tableSpec.readHuffmanCodes (lis);
            break;
          case DQT : // define quantization table
            tableSpec.readQuantizationTables (lis);
            break;
          case DRI : // define restart interval
            Assertions.expect (length,2);
            restartInterval = dis.readUnsignedShort ();
            if (Options.tracing)
              System.err.println ("restart interval : " + restartInterval);
            break;
          case SOF0 :
          case SOF2 :
            Assertions.expect (!afterSOF);
            afterSOF = true;
            mode = segmentType;
            // start of frame : image format
            int precision = dis.read ();
            Assertions.expect (precision,8);
            height = dis.readShort ();
            width = dis.readShort ();
            int ncomponent = dis.read ();
            components = new JPEGComponent [ncomponent];
            Assertions.limit (ncomponent,1,4);

            for (int i = 0;i < ncomponent;i++)
              {
                int id = dis.read ();
                Integer previous = (Integer) indices.get (new Integer (id));
                if (previous != null)
                  {
                    // 2002007128-Screen_1.pdf contains a CMYK image
                    // that has IDs 1,2,3,1 -- this is a workaround.
                    Assertions.expect (i,3);
                    Assertions.expect (id,1);
                    Assertions.expect (previous.intValue (),0);
                    Assertions.expect (ncomponent,4);
                    Assertions.expect (segmentType,SOF0);
                    id = 4;
                    buggyIDs = true;
                  }

                indices.put (new Integer (id),new Integer (i));

                if (Options.tracing)
                  System.out.println ("id : " + id);

                int samp = dis.read ();
                int hSamp = (samp >> 4) & 0xf;
                int vSamp = (samp     ) & 0xf;

                if (ncomponent == 1) // greyscale
                  /* I don't really understand this. JPEG.txt says that
                     you don't need to worry about MCUs for grayscale,
                     and just read 8x8 blocks. One grayscale image has
                     hsamp = vsamp = 2 -- I don't know what this information
                     could mean; the image comes out right if we disregard
                     it and read 8x8 blocks sequentially as JPEG.txt suggests. */
                  hSamp = vSamp = 1;
                int tableNumber = dis.read ();
                if (Options.tracing)
                  System.out.println ("component " + id + " uses slot " + tableNumber);
                components [i] = new JPEGComponent (id,hSamp,vSamp,tableNumber);
              }
            return;
          case SOS :
            endOfScan = false;
            beginRestarts ();
            JPEGComponent component = layout.startScan (this,in,buggyIDs);
            switch (mode)
              {
              case SOF0 :
                Assertions.expect (component,null);

                JPEGRequest request = new JPEGRequest (RAW);
    
                for (int y = 0;y < mcuHeight;y++)
                  for (int x = 0;x < mcuWidth;x++)
                    {
                      restart ();
                      request.x = x;
                      request.y = y;
                      sink.allocationRequest (request);
                      MCU mcu = format.mcus [y] [x];
                      mcu.clear ();
                      layout.readRawData (mcu);
                      sink.writeRequest (request);
                    }
                break;
              case SOF2 :
                if (component != null)
                  {
                    int height = partsOf (8,format.pixelHeight);
                    int width  = partsOf (8,format.pixelWidth);
                    int vsamp = component.vSamp;
                    int hsamp = component.hSamp;
                    if (vsamp != hsamp)
                      throw new NotTestedException ();
                    for (int y = 0;y < height;y++)
                      for (int x = 0;x < width;x++)
                        {
                          restart ();
                          layout.huffmanDecode (format.mcus [y / vsamp] [x / hsamp],0,
                                                (y % vsamp) * hsamp + (x % hsamp)); 
                        }
                  }
                else
                  // full or DC scan
                  for (int y = 0;y < mcuHeight;y++)
                    for (int x = 0;x < mcuWidth;x++)
                      {
                        restart ();
                        layout.readRawData (format.mcus [y] [x]);
                      }
                break;
              default : throw new NotImplementedException ("JPEG mode " + (mode - SOF0));
              }

            Assertions.expect (layout.nEOB,0);
            break;
          case COM :
            comment = Util.readString (dis,length);
            break;
            // application data
          case APP0 :
          case APP1 :
          case APP12 :
            lis.skipToLimit ();
            break;
          case APP13 :
            if (length >= 9)
              {
                if (new String (Util.readBytes (dis,9)).equals ("Photoshop"))
                  {
                    Assertions.expect (!afterSOF);
                    invertCMYK = true;
                  }
                dis.skipBytes (length - 9);
              }
            else
              lis.skipToLimit ();
            break;
          case APP2 :
            if (length >= 14)
              {
                Assertions.expect (!afterSOF);
                byte [] tag = Util.readBytes (dis,11);
                int zero = dis.read ();
                if (new String (tag).equals ("ICC_PROFILE") && zero == 0)
                  {
                    // We're not supposed to rely on the blocks being in order
                    int index = dis.read ();
                    int count = dis.read ();
                    if (iccData == null)
                      iccData = new byte [count] [];
                    else
                      Assertions.expect (count,iccData.length);
                    Assertions.limit (index,1,count);
                    iccData [index - 1] = Util.readBytes (dis,length - 14);
                  }
                else
                  dis.skipBytes (length - 12);
              }
            else
              lis.skipToLimit ();
            break;
          case APP14 : // Adobe data
            // examples of files with longer Adobe markers:
            // 0813532566_1.pdf, 0813532590-ae.pdf
            Assertions.expect (length >= 12);
            Assertions.expect (Util.readString (dis,5),"Adobe");
            dis.readShort (); // version
            dis.readShort (); // flags 0
            dis.readShort (); // flags 1
            // colorTransform is used once SOF has been read
            Assertions.expect (!afterSOF);
            colorTransform = dis.readByte ();
            Util.skipBytesExactly (dis,length - 12);
            break;
          default :
            throw new NotImplementedException
              ("segment type " + Integer.toHexString (segmentType));
          }
      }
  }

  static private int partsOf (int small,int big)
  {
    return (big - 1) / small + 1;
  }

  int nextMarker () throws IOException
  {
    for (;;)
      {
	int b;
	do
	  if ((b = in.read ()) == -1)
	    return b;
	while (b != ESC);
	if ((b = in.read ()) != 0)
	  return b;
      }
  }

  boolean waitingForRestart;
  int restartCount;
  int mcuCount;

  void beginRestarts ()
  {
    waitingForRestart = true;
    restartCount = 0;
    mcuCount = 0;
  }

  void handleRestartMarker (int b)
  {
    Assertions.expect (b - RST0,restartCount);
    restartCount++;
    restartCount &= 7;
  }

  void restart () throws IOException
  {
    if (mcuCount == restartInterval)
      mcuCount = 0;
    if (mcuCount++ == 0)
      {
        if (waitingForRestart)
          waitingForRestart = false;
        else
          handleRestartMarker (nextMarker ());

        bitBuffer.reset ();
        layout.restartInput ();
      }
  }

  void scan () throws IOException
  {
    switch (mode)
      {
      case SOF0 :
        handleSegments ();
        break;
      case SOF2 : 
        JPEGRequest request = new JPEGRequest (RAW,mcuWidth,mcuHeight);
        sink.allocationRequest (request);
        for (int y = 0;y < mcuHeight;y++)
          for (int x = 0;x < mcuWidth;x++)
            format.mcus [y] [x].clear ();
        handleSegments ();
        sink.writeRequest (request);
        break;
      default : throw new NotImplementedException ("JPEG mode " + (mode - SOF0));
      }
    Assertions.expect (layout.isComplete ());
  }

  public void discard () throws IOException
  {
    setSink (new JPEGBuffer (format));
    scan ();
  }

  public void rewriteTo (OutputStream out) throws IOException
  {
    JPEGWriter writer = new JPEGWriter ();
    JPEGBuffer buffer = new JPEGBuffer (format);
    setSink (Concatenator.concatenate (buffer,writer).getSink ());
    writer.setFormat (format);
    writer.setSink (out);
    scan ();
  }

  public Dimension getImageSize ()
  {
    return new Dimension (width,height);
  }

  public int getComponentCount ()
  {
    return layout.components.length;
  }

  public ColorModel getColorModel ()
  {
    if (iccData != null) {
      int pos = 0;
      for (int i = 0;i < iccData.length;i++)
        pos += iccData [i].length;
      byte [] data = new byte [pos];
      pos = 0;
      for (int i = 0;i < iccData.length;i++) {
        byte [] block = iccData [i];
        System.arraycopy (block,0,data,pos,block.length);
        pos += block.length;
      }
      return new ICCColorModel (data,false,invertCMYK);
    }

    switch (getComponentCount ())
    {
    case 1 : return new  GrayColorModel (false);
    case 2 : return new DummyColorModel (2,false);
    case 3 : return new   RGBColorModel (false);
    case 4 : return new  CMYKColorModel (invertCMYK);
    default : throw new Error ("too many JPEG components");
    }
  }
}
