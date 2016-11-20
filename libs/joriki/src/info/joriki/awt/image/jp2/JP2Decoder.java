/*
 * Copyright 2003 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.jp2;

import java.io.IOException;
import java.io.EOFException;
import java.io.DataInputStream;
import java.io.StreamCorruptedException;

import java.awt.Dimension;
import java.awt.Rectangle;

import java.awt.image.ColorModel;

import java.util.Arrays;

import info.joriki.io.Util;
import info.joriki.io.BitSource;
import info.joriki.io.CountingInputStream;

import info.joriki.awt.image.FancyPaletteColorModel;
import info.joriki.awt.image.ICCColorModel;
import info.joriki.awt.image.RGBColorModel;
import info.joriki.awt.image.GrayColorModel;
import info.joriki.awt.image.CMYKColorModel;
import info.joriki.awt.image.SRGBColorModel;
import info.joriki.awt.image.SGrayColorModel;
import info.joriki.awt.image.StreamingImageDecoder;

import info.joriki.util.General;
import info.joriki.util.Options;
import info.joriki.util.Assertions;
import info.joriki.util.NotTestedException;
import info.joriki.util.NotImplementedException;

public class JP2Decoder extends StreamingImageDecoder implements JP2Speaker, BitSource
{
  final static int [] ncontexts = {5,9,3};
  final static int [] [] contextBorders = {{
    (0 * 3 + 0) * 5 + 1,
    (0 * 3 + 0) * 5 + 2,
    (0 * 3 + 1) * 5 + 0,
    (0 * 3 + 2) * 5 + 0,
    (1 * 3 + 0) * 5 + 0,
    (1 * 3 + 0) * 5 + 1,
    (1 * 3 + 1) * 5 + 0,
    (2 * 3 + 0) * 5 + 0,
    (3 * 3 + 0) * 5 + 0
  },{
    0 * 5 + 1,
    0 * 5 + 2,
    1 * 5 + 0,
    1 * 5 + 1,
    1 * 5 + 2,
    2 * 5 + 0,
    2 * 5 + 1,
    3 * 5 + 0,
    5 * 5 + 0
  }};

  CountingInputStream cis;
  DataInputStream dis;

  int ncomponents;
  int colorSpaceUnknown;
  int colorSpace;

  boolean haveColorSpecification;
  ICCColorModel iccColorModel;
  
  int xTileSize;
  int yTileSize;
  int xBlockSize;
  int yBlockSize;

  BitDescriptor bitDescriptor;
  BitDescriptor [] bitDescriptors;

  int [] xSampling;
  int [] ySampling;

  Tile [] tiles;
  Tile tile;

  TileStyle tileStyle;
  TileStyle defaultTileStyle;
  CodingStyle [] codingStyles;
  CodingStyle [] defaultCodingStyles;
  QuantizationStyle [] quantizationStyles;
  QuantizationStyle [] defaultQuantizationStyles;

  BitReader bitReader = new BitReader ();
  ArithmeticDecoder arithmeticDecoder = new ArithmeticDecoder ();

  ArithmeticCodingContext [] [] contexts = new ArithmeticCodingContext [ncontexts.length] [];
  ArithmeticCodingContext [] [] subbandContexts = new ArithmeticCodingContext [2] [45];
  ArithmeticCodingContext runlengthContext = new ArithmeticCodingContext (3);
  ArithmeticCodingContext uniformContext = new ArithmeticCodingContext (46);
  ArithmeticCodingContext [] signContexts = new ArithmeticCodingContext [25];
  ArithmeticCodingContext [] magnitudeContexts;
  ArithmeticCodingContext [] significanceContexts;

  public JP2Decoder (Object source)
  {
    setSource (source);

    for (int i = 0;i < contexts.length;i++)
    {
      contexts [i] = new ArithmeticCodingContext [ncontexts [i]];
      for (int j = 0;j < contexts [i].length;j++)
        contexts [i] [j] = new ArithmeticCodingContext (i == SIGNIFICANCE && j == 0 ? 4 : 0);
    }

    int label;
    int index;

    index = label = 0;
    for (int i = 0;i < 3;i++)
      for (int j = 0;j < 3;j++)
        for (int k = 0;k < 5;k++)
        {
          if (index >= contextBorders [0] [label])
            label++;
          subbandContexts [0] [index++] = contexts [SIGNIFICANCE] [label];
        }
	    
    index = label = 0;
    for (int k = 0;k < 5;k++)
      for (int sum = 0;sum < 5;sum++)
      {
        if (index++ >= contextBorders [1] [label])
          label++;
        for (int i = 0,j = sum;j >= 0;i++,j--)
          if (i <= 2 && j <= 2)
            subbandContexts [1] [(i * 3 + j) * 5 + k] = contexts [SIGNIFICANCE] [label];
      }

    index = 0;
    for (int i = -2;i <= 2;i++)
      for (int j = -2;j <= 2;j++)
        signContexts [index++] = contexts [SIGN] [Math.abs (3 * General.clip (i,-1,1) + General.clip (j,-1,1))];

    magnitudeContexts = contexts [MAGNITUDE];
  }

  void resetDecodingContexts ()
  {
    for (int i = 0;i < contexts.length;i++)
      for (int j = 0;j < contexts [i].length;j++)
        contexts [i] [j].reset ();
    runlengthContext.reset ();
    uniformContext.reset ();
  }

  byte [] [] paletteColors;

  private int getComponentCount () {
    if (iccColorModel != null)
      return iccColorModel.getNumComponents ();

    switch (colorSpace) {
    case CMYK : return 4;
    case sRGB : return 3;
    case sGRAY: return 1;
    default : throw new NotImplementedException ("enumerated color space " + colorSpace);
    }
  }
  
  private ColorModel getColorModel (int ncomponents) {
    if (!haveColorSpecification)
      switch (ncomponents)
      {
      case 1 : return new GrayColorModel (false);
      case 3 : return new RGBColorModel (false);
      default: throw new NotImplementedException ("unidentified color space with " + ncomponents + " components");
      }

    Assertions.expect (ncomponents,getComponentCount ());

    if (iccColorModel != null)
      return iccColorModel;

    switch (colorSpace)
    {
    case CMYK :
      Assertions.expect (colorSpaceUnknown,1);
      return new CMYKColorModel ();
    case sRGB :
      switch (colorSpaceUnknown)
      {
      case 0 : return SRGBColorModel.sRGB;
      case 1 : return new RGBColorModel (false);
      default: throw new NotImplementedException ("color space indicator " + colorSpaceUnknown);
      }
    case sGRAY :
      switch (colorSpaceUnknown)
      {
      case 0 : return SGrayColorModel.sGRAY;
      case 1 : return new GrayColorModel (false);
      default: throw new NotImplementedException ("color space indicator " + colorSpaceUnknown);
      }
    default :
      throw new NotImplementedException ("enumerated color space " + colorSpace);
    }
  }
  
  ColorModel getColorModel ()
  {
    if (paletteColors != null)
    {
      int [] cmap = new int [paletteColors [0].length];
      for (int i = 0;i < cmap.length;i++)
        for (int j = 0;j < paletteColors.length;j++) {
          cmap [i] <<= 8;
          cmap [i] |= paletteColors [j] [i];
        }
      return new FancyPaletteColorModel (getColorModel (paletteColors.length),cmap);
    }
    return getColorModel (ncomponents);
  }

  protected void read () throws IOException
  {
    cis = new CountingInputStream (in);
    dis = new DataInputStream (cis);

    decodeJP2Headers ();
    
    if (bitDescriptor.depth != 8 && paletteColors == null)
      throw new NotTestedException ("non-byte JPEG2000 without palette");

    decodeCodeStream ();
  }

  public void discard () throws IOException
  {
    read ();
  }

  protected void produceStaticImage () throws IOException
  {
    read ();
    decodePacketData ();

    colorModel = getColorModel ();
    setParameters ();

    for (int i = 0;i < tiles.length;i++)
    {
      Tile tile = tiles [i];
      tile.perform (INVERSETRANSFORM);
      tile.transformColors ();
      tile.perform (SHIFT);
      tile.perform (TOBYTES);
      if (ncomponents == 1)
        setPixels (tile.x,tile.y,tile.width,tile.height,tile.getBytes (),tile.width);
      else
        setPixels (tile.x,tile.y,tile.width,tile.height,tile.getInts (),tile.width);
    }
  }

  boolean isJP2 = false;

  void decodeJP2Headers () throws IOException
  {
    for (;;)
    {
      // split up length = dis.readInt () to detect raw, non-JP2 file
      int hi = dis.readUnsignedShort ();
      if (!isJP2 && hi == ((0xff << 8) | SOC))
        return;
      isJP2 = true;
      int length = (hi << 16) | dis.readUnsignedShort ();

      int type = dis.readInt ();

      if (Options.tracing)
        System.out.println ("type : " + Integer.toHexString (type));

      if (length == 1)
        throw new NotImplementedException ("extended box length");
	
      length -= 8;

      switch (type)
      {
      case jP :
        Assertions.expect (dis.readInt (),0x0d0a870a);
        break;
      case ftyp :
        Assertions.expect (dis.readInt (),jp2);
        dis.skipBytes (length - 4);
        break;
      case ihdr :
        height = dis.readInt ();
        width = dis.readInt ();
        ncomponents = dis.readUnsignedShort ();
        int code = dis.readUnsignedByte ();
        if (code == 255)
          throw new NotImplementedException ("component-dependent bit depth");
        bitDescriptor = new BitDescriptor (code);
        // in this case, ihdr, pclr and SIZ may need to be adapted
        if (bitDescriptor.depth > 8)
          throw new NotImplementedException ("multi-byte JPEG2000 samples");
        int compressionType = dis.readUnsignedByte ();
        Assertions.expect (compressionType,7);
        colorSpaceUnknown = dis.readUnsignedByte ();
        dis.readUnsignedByte (); // intellectual property rights
        break;
      case colr :
        if (haveColorSpecification) {
          dis.skipBytes (length);
          break;
	}
        int method = dis.readUnsignedByte ();
        dis.readUnsignedByte (); // precedence
        dis.readUnsignedByte (); // approximation
        switch (method)
	      {
	      case 1 :
          colorSpace = dis.readInt ();
          break;
	      case 2 :
	  iccColorModel = new ICCColorModel (Util.readBytes (dis,length - 3));
	  break;
	      default :
          throw new NotImplementedException ("color space method " + method);
	      }
        haveColorSpecification = true;
        break;
      case pclr :
        Assertions.expect (ncomponents,1);
        int nentries = dis.readUnsignedShort ();
        Assertions.limit (nentries,0,256);
        int ncolumns = dis.readUnsignedByte ();
        Assertions.expect (ncolumns,getComponentCount ());
        for (int i = 0;i < ncolumns;i++)
          // This is supposed to be the bit depth of the palette entries.
          // The standard says nothing about how to interpret non-byte values.
          // I would assume we would need to scale them, but jasper doesn't.
          // As long as we're only interested in JPEG2000 images in PDF files,
          // we're not actually using the palette, so we don't need to worry
          // about that and only have to make sure we're reading the right
          // number of bytes.
          // isixsigma20071112_34.pdf contains two images (Im12 and Im15)
          // with 4-bit palette indices. It specifies a bit depth of 4 for
          // the palette entries but the entries are actually 8-bit. These
          // images crash QuickTimePlayer, which can otherwise display
          // JPEG2000 images, so they're probably invalid. Again, we don't
          // need to worry about this as long as we're not using the palette.
          if (readBitDescriptor ().depth > 8)
            throw new NotImplementedException ("multi-byte JPEG2000 palette");
        int actualEntryCount = length - (3 + ncolumns);
        if (actualEntryCount % ncolumns != 0)
          throw new Error ("JPEG2000 palette incomplete");
        actualEntryCount /= ncolumns;
        if (actualEntryCount != nentries)
          Options.warn ("JPEG2000 palette too short");
        paletteColors = new byte [ncolumns] [nentries];
        for (int i = 0;i < actualEntryCount;i++)
          for (int j = 0;j < ncolumns;j++)
            paletteColors [j] [i] = dis.readByte ();
        break;
      case cmap :
        // We have encountered two types of cmap boxes so far.
        // A "regular" cmap box specifies default palette mapping:
        // Each channel is derived using the only component of the
        // code stream to index the appropriate component of the palette.
        // However, for indexed CMYK images there are also cmap boxes that
        // do this for the first channel only. The remaining values are all
        // zero, except that sometimes (e.g. /Im0 in chron20071005A_6.pdf)
        // the component number for the third channel has an invalid value.
        // In one case (/Im1 in chron20071005A_6.pdf), the component numbers
        // of the second and fourth channel also have invalid values.
        // ghostscript also complains about the invalid component numbers.
        // Since we are currently not using the color space specified in
        // JPEG2000 files, we don't really care about the component mapping
        // box; once we do (i.e. if we encounter a JPEG2000 image without
        // a ColorSpace entry in the image dictionary), this check may need
        // to be made more rigorous.
        for (int i = 0;i < paletteColors.length;i++)
	      {
          int component = dis.readUnsignedShort ();
          int mapping   = dis.readUnsignedByte ();
          int channel   = dis.readUnsignedByte ();
          if (component != 0 || // only component
              mapping != 1 ||   // palette mapping
              channel != i) {   // identity mapping
            if (i == 0)
              throw new NotImplementedException ("non-trivial component mapping");
            Options.warn ("unknown or invalid component mapping");
          }
	      }
        break;
      case xml :
        System.out.println ("embedded XML data : " + Util.readString (dis,length).replace ('\r','\n'));
        break;
      case jp2c :
        Assertions.expect (dis.readUnsignedByte (),0xff);
        Assertions.expect (dis.readUnsignedByte (),SOC);
        return;
        // superboxes
      case jp2h :
        break;
        // ignored unknown boxes
      case rreq :
        dis.skipBytes (length);
        break;
      default :
        throw new NotImplementedException ("box type " + Integer.toHexString (type));
      }
    }
  }

  // states
  final static int DATA = -1;
  final static int PART = -2;
  final static int MAIN = -3;
  
  void decodeCodeStream () throws IOException
  {
    int partEnd = 0;
    int state = SIZ;

    for (;;)
    {
      Assertions.expect (dis.readUnsignedByte (),0xff);
      int marker = dis.readUnsignedByte ();
      if (Options.tracing)
        System.out.println ("marker " + Integer.toHexString (marker));

      switch (marker)
      {
      case SOC :
        throw new StreamCorruptedException ("mutiple SOC markers");
      case EOC :
        Assertions.expect (state,DATA);
        return;
      case SOD :
        Assertions.expect (state,PART);
        Assertions.expect (width < 1 << 15);  // default precinct size:
        Assertions.expect (height < 1 << 15); // only one precinct
        if (tile.components == null)
          tile.initialize (bitDescriptors,codingStyles,quantizationStyles,tileStyle);
        while (cis.getCount () < partEnd)
        {
          tile.readPacket (this,dis);
          nbit = bits = 0;
        }
        Assertions.expect (cis.getCount (),partEnd);
        state = DATA;
        continue;
      }

      int end = cis.getCount ();
      int length = dis.readUnsignedShort ();
      end += length;
      int component;
      int nbytes;

      switch (marker)
      {
      case SIZ :
        Assertions.expect (state,SIZ);
        dis.readUnsignedShort (); // capabilities
        int xSize = dis.readInt ();
        int ySize = dis.readInt ();
        int xOffset = dis.readInt ();
        int yOffset = dis.readInt ();
        xTileSize = dis.readInt ();
        yTileSize = dis.readInt ();
        int xTileOffset = dis.readInt ();
        int yTileOffset = dis.readInt ();

        int cSize = dis.readUnsignedShort ();

        if (xOffset != 0 || yOffset != 0)
          throw new NotImplementedException ("image offset");
        // this would lead to odd transforms
        if (!(((xTileSize & 1) == 0 || xTileSize == xSize) &&
              ((yTileSize & 1) == 0 || yTileSize == ySize)))
          throw new NotImplementedException ("odd tile size");
        if (xTileOffset != 0 || yTileOffset != 0)
          throw new NotImplementedException ("tile offset");

        Assertions.expect (xTileSize + xTileOffset > xOffset);
        Assertions.expect (yTileSize + yTileOffset > yOffset);

        if (isJP2)
        {
          Assertions.expect (cSize,ncomponents);
          Assertions.expect (xSize,width);
          Assertions.expect (ySize,height);
        }
        else
        {
          ncomponents = cSize;
          width =  xSize;
          height = ySize;
        }

        codingStyles = new CodingStyle [ncomponents];
        quantizationStyles = new QuantizationStyle [ncomponents];
        bitDescriptors = new BitDescriptor [ncomponents];
        xSampling =  new int [ncomponents];
        ySampling =  new int [ncomponents];

        for (int i = 0;i < ncomponents;i++)
        {
          bitDescriptors [i] = readBitDescriptor ();
          xSampling [i] = dis.readUnsignedByte ();
          ySampling [i] = dis.readUnsignedByte ();
          Assertions.expect (bitDescriptors [i],bitDescriptor);
          if (xSampling [i] * ySampling [i] != 1)
            throw new NotImplementedException ("component subsampling");
        }

        PatchIterator tileIterator = new PatchIterator
        (new Rectangle (0,0,xSize,ySize),new Dimension (xTileSize,yTileSize),true);
        tiles = new Tile [tileIterator.getCount ()];
        for (int i = 0;i < tiles.length;i++)
          tiles [i] = new Tile (tileIterator.nextPatch ());
        state = MAIN;
        break;
      case COD :
        Assertions.expect (state == MAIN || state == PART);
        int flags = dis.readUnsignedByte ();
        tileStyle = new TileStyle (dis,flags);
        CodingStyle codingStyle = new CodingStyle (dis,flags);
        for (int i = 0;i < codingStyles.length;i++)
          codingStyles [i] = codingStyle;
        break;
      case COC : 
        Assertions.expect (state == MAIN || state == PART);
        nbytes = ncomponents > 256 ? 2 : 1;
        component = Util.readInteger (dis,nbytes);
        codingStyles [component] =
          new CodingStyle (dis,dis.readUnsignedByte ());
        break;
      case QCD :
        Assertions.expect (state == MAIN || state == PART);
        QuantizationStyle quantizationStyle =
          new QuantizationStyle (dis,length - 3);
        for (int i = 0;i < quantizationStyles.length;i++)
          quantizationStyles [i] = quantizationStyle;
        break;
      case QCC :
        Assertions.expect (state == MAIN || state == PART);
        nbytes = ncomponents > 256 ? 2 : 1;
        component = Util.readInteger (dis,nbytes);
        quantizationStyles [component] =
          new QuantizationStyle (dis,length - 3 - nbytes);
        break;
      case RGN :
        Assertions.expect (state == MAIN || state == PART);
        throw new NotImplementedException ("region of interest");
      case SOT :
        switch (state)
        {
        case MAIN : // first SOT
          defaultCodingStyles = codingStyles;
          defaultQuantizationStyles = quantizationStyles;
          defaultTileStyle = tileStyle;
          break;
        case DATA :
          break;
        default :
          throw new Error ("invalid segment order");
        }

        codingStyles = defaultCodingStyles.clone ();
        quantizationStyles = defaultQuantizationStyles.clone ();
        tileStyle = defaultTileStyle;

        tile = tiles [dis.readUnsignedShort ()];
        int partLength = dis.readInt ();
        Assertions.unexpect (partLength,0); // part is rest of file
        int partIndex = dis.readUnsignedByte ();
        Assertions.expect (partIndex,tile.partIndex++);
        int nparts = dis.readUnsignedByte ();
        if (nparts != 0)
	      {
          if (partIndex == 0)
            tile.nparts = nparts;
          else
            Assertions.expect (nparts,tile.nparts);
          Assertions.limit (partIndex,0,tile.nparts);
          if (partIndex == tile.nparts)
            Options.warn ("invalid tile part count in JPEG2000 file");
	      }
        partEnd = cis.getCount () + partLength - (length + 2);
        state = PART;
        break;
      case COM :
        Assertions.expect (state == MAIN || state == PART);
        int registration = dis.readUnsignedShort ();
        Assertions.limit (registration,0,1);
        System.out.println ("JPEG 2000 comment : " + Util.readString (dis,length - 4));
        break;
      default :
        throw new NotImplementedException ("marker " + Integer.toHexString (marker));
      }
      Assertions.expect (cis.getCount (),end);
    }
  }

  void decodePacketData () throws IOException
  {
    for (int i = 0;i < tiles.length;i++)
    {
      Tile tile = tiles [i];
      for (int j = 0;j < tile.components.length;j++)
      {
        TileComponent tileComponent = tile.components [j];
        CodingStyle codingStyle = tileComponent.codingStyle;
        QuantizationStyle quantizationStyle = tileComponent.quantizationStyle;
        Dimension blockSize = codingStyle.blockSize;
        int scan = blockSize.width + 2;
        int size = (blockSize.width + 2) * (blockSize.height + 2);
        short [] magnitude = new short [size];
        short [] states = new short [size];
        float [] floatBuf = tileComponent.floatBuf;
        int   [] intBuf = tileComponent.intBuf;
        boolean integer = quantizationStyle.quantizationStyle == NO_QUANTIZATION;

        for (int level = 0;level <= codingStyle.nlevels;level++)
	      {
          ResolutionLevel resolutionLevel = tileComponent.resolutionLevels [level];
          for (int k = 0;k < resolutionLevel.precincts.length;k++)
          {
            Precinct precinct = resolutionLevel.precincts [k];
            for (int suby = 0,sub = 0;suby < 2;suby++)
              for (int subx = 0;subx < 2;subx++)
                if ((subx == 0 && suby == 0) == (level == 0))
                {
                  Band band = precinct.bands [sub++];
                  // strangely, the exponent seems to drop out of the
                  // dequantization prescription for the irreversible case
                  int leftShift = quantizationStyle.guardBits +
                  (integer ? quantizationStyle.exponent [level] [subx] [suby] :
                    tileComponent.bitDescriptor.depth + subx + suby);
                  float mantissa = integer ? 0 : quantizationStyle.mantissa [level] [subx] [suby];
                  boolean HL = subx == 1 && suby == 0;
                  significanceContexts = subbandContexts [subx * suby];

                  for (int y = 0;y < band.blockHeight;y++)
                    for (int x = 0;x < band.blockWidth;x++)
                    {
                      Block block = band.blocks [x] [y];
                      arithmeticDecoder.segments = bitReader.segments = block.segments.iterator ();
                      block.segments = null; // for GC

                      resetDecodingContexts ();
                      Arrays.fill (states,(short) (SIGNOFFSET << SIGNSHIFT));

                      int w = block.width;
                      int h = block.height;

                      for (int pass = 0;pass < block.npasses;pass++)
                      {
                        BitReader reader;
                        int signThreshold;
                        if (codingStyle.bypass (pass))
                        {
                          reader = bitReader;
                          signThreshold = 0;
                        }
                        else
                        {
                          reader = arithmeticDecoder;
                          signThreshold = SIGNOFFSET;
                        }
                        if (codingStyle.initial (pass))
                          reader.initialize ();
                        if (codingStyle.resetProbabilities)
                          resetDecodingContexts ();

                        int type = pass % 3;

                        if (type == SIGNIFICANCE)
                          for (int l = 0;l < states.length;l++)
                            states [l] &= ~DONE;

                        for (int u = 0;u < h;u += 4)
                        {
                          int n = Math.min (h - u,4);
                          for (int t = 0;t < w;t++)
                          {
                            int start = (u + 1) * scan + t + 1;
                            int limit = start + n * scan;
                            int index = start;
                            boolean decision = false;
                            if (type == CLEANUP && n == 4)
                            {
                              int bits = 0;
                              for (int l = start;l < limit;l += scan)
                                bits |= states [l];
                              if ((bits & (CONTEXT | DONE)) == 0)
                              {
                                if (!reader.readBit (runlengthContext))
                                  continue;
                                for (int bit = 2;bit > 0;bit--)
                                  if (reader.readBit (uniformContext))
                                    index += bit * scan;
                                decision = true;
                              }
                            }

                            for (;index < limit;index += scan)
                            {
                              short state = states [index];

                              if (type == MAGNITUDE)
                              {
                                if ((state & DONE) != 0 ||
                                    (state & SIGNIFICANT) == 0)
                                  continue;
                                magnitude [index] <<= 1;
                                if (reader.readBit
                                    (magnitudeContexts
                                     [(state & REFINED) != 0 ? 2 :
                                      (state & CONTEXT) != 0 ? 1 : 0]))
                                  magnitude [index] |= 1;
                                state |= REFINED;
                              }
                              else
                              {
                                if (!decision)
                                {
                                  if (type == CLEANUP ?
                                      (state & DONE) != 0 :
                                      (state & CONTEXT) == 0 ||
                                      (state & SIGNIFICANT) != 0)
                                    continue;

                                  decision = reader.readBit (significanceContexts [state & CONTEXT]);
                                }

                                if (decision)
                                {
                                  decision = false;

                                  state |= SIGNIFICANT;
                                  magnitude [index] = 1;

                                  int sign = 1 << SIGNSHIFT;
                                  int hash = (state & SIGNMASK) >> SIGNSHIFT;
                                  if (reader.readBit (signContexts [hash]) ^ (hash < signThreshold))
                                  {
                                    state |= SIGNFLAG;
                                    sign = -sign;
                                  }

                                  int horizontal = 5 * sign + (HL ? 5 : 5 * 3);
                                  int vertical   =     sign + (HL ? 5 * 3 : 5);

                                  states [index + scan - 1]++;
                                  states [index + scan + 0] += vertical;
                                  states [index + scan + 1]++;

                                  states [index + 1] += horizontal;
                                  states [index - 1] += horizontal;

                                  if (!(codingStyle.verticallyCausalContext && index == start))
                                  {
                                    states [index - scan + 1]++;
                                    states [index - scan + 0] += vertical;
                                    states [index - scan - 1]++;
                                  }
                                }
                              }
                              states [index] = (short) (state | DONE);
                            }
                          }
                        }
                        if (type == CLEANUP && codingStyle.segmentationSymbols)
                        {
                          boolean toggle = true;
                          for (int n = 0;n < 4;n++,toggle = !toggle)
                            Assertions.expect (reader.readBit (uniformContext),toggle);
                        }
                      }

                      boolean adjustMagnitudes = (block.npasses % 3) == MAGNITUDE;
                      int rightShift = (block.npasses + 4) / 3; // decoded bit planes
                      Assertions.expect (rightShift < 16); // fit magnitude in short
                      rightShift += block.zeroBitPlanes;
                      rightShift++; // for 1 in equation E.2
                      rightShift++; // for shift to add 1/2 below
                      float scale = (1 << leftShift) * mantissa / (1 << rightShift);
                      int totalShift = leftShift - rightShift;
                      rightShift = -totalShift;

                      for (int v = 0;v < h;v++)
                      {
                        int index = (v + 1) * scan + 1;
                        int tileIndex = (block.y + v) * tileComponent.width + block.x;
                        for (int t = 0;t < w;t++,index++,tileIndex++)
                        {
                          short state = states [index];
                          if ((state & SIGNIFICANT) != 0)
                          {
                            int mag = magnitude [index];
                            if (adjustMagnitudes && (state & DONE) == 0)
                              // make up for missing magnitude refinement pass
                              mag <<= 1;
                            mag <<= 1;
                            mag++;
                            if (integer) {
                              if (totalShift < 0)
                                mag >>= rightShift;
                              else
                                mag <<= totalShift;
                            }
                            if ((state & SIGNFLAG) != 0)
                              mag = -mag;
                            if (integer)
                              intBuf [tileIndex] = mag;
                            else
                              floatBuf [tileIndex] = mag * scale;
                          }
                        }
                      }
                    }
                }
          }
        }
      }
    }
  }

  int bits;
  int nbit;

  public int readBits (int n) throws IOException
  {
    while (n > nbit)
    {
      bits <<= 8;
      int next = cis.read ();
      if (next < 0)
        throw new EOFException ();
      bits |= next;
      nbit += 8;
      if (next == 0xff)
      {
        bits <<= 7;
        next = cis.read ();
        if (next < 0 || (next & 0x80) != 0)
          throw new StreamCorruptedException ();
        bits |= next;
        nbit += 7;
      }
    }
    return (bits >> (nbit -= n)) & ((1 << n) - 1);
  }

  BitDescriptor readBitDescriptor () throws IOException {
    return new BitDescriptor (dis.readUnsignedByte ());
  }
}
