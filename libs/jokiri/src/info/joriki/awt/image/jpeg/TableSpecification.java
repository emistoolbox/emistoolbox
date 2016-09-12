/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.jpeg;

import java.io.IOException;
import java.io.InputStream;

import info.joriki.io.Resources;
import info.joriki.io.Readable;
import info.joriki.io.Writeable;

import info.joriki.io.filter.Concatenator;
import info.joriki.io.filter.InputFilter;
import info.joriki.io.filter.OutputFilter;

import info.joriki.awt.image.CartesianTransform;

import info.joriki.util.Assertions;

import info.joriki.compression.huffman.HuffmanCode;
import info.joriki.compression.huffman.HuffmanDecoder;
import info.joriki.compression.huffman.HuffmanEncoder;

class TableSpecification implements JPEGSpeaker
{
  // second index : ACDC
  HuffmanCode [] [] huffmanCodes = new HuffmanCode [4] [2];
  Readable [] [] huffmanSources;
  Writeable [] [] huffmanSinks;

  QuantizationTable [] quantizationTables = new QuantizationTable [4];

  TableSpecification () {}

  TableSpecification (String name) throws IOException {
    readQuantizationTables (name);
  }

  
  TableSpecification (TableSpecification tableSpecification) {
    for (int i = 0;i < 4;i++)
    {
      for (int j = 0;j < 2;j++)
        huffmanCodes [i] [j] = tableSpecification.huffmanCodes [i] [j];
      quantizationTables [i] = tableSpecification.quantizationTables [i];
    }
  }
  
  TableSpecification transformedBy (CartesianTransform transform)
  {
    TableSpecification transformed = new TableSpecification ();

    for (int i = 0;i < 4;i++)
      {
        for (int j = 0;j < 2;j++)
          transformed.huffmanCodes [i] [j] = huffmanCodes [i] [j];
        if (quantizationTables [i] != null)
          transformed.quantizationTables [i] =
            quantizationTables [i].transformedBy (transform);
      }

    return transformed;
  }

  InputFilter bytesToBits;

  void setSource (InputFilter bytesToBits)
  {
    if (bytesToBits != this.bytesToBits)
      huffmanSources = new Readable [4] [2];
    this.bytesToBits = bytesToBits;
  }

  OutputFilter bitsToBytes;

  void setSink (OutputFilter bitsToBytes)
  {
    if (bitsToBytes != this.bitsToBytes)
      huffmanSinks = new Writeable [4] [2];
    this.bitsToBytes = bitsToBytes;
  }

  Readable getHuffmanSource (int codeNumber,int acdc)
  {
    if (huffmanSources [codeNumber] [acdc] == null &&
        huffmanCodes   [codeNumber] [acdc] != null)
      huffmanSources [codeNumber] [acdc] = (Readable) Concatenator.concatenate (bytesToBits,new HuffmanDecoder (huffmanCodes [codeNumber] [acdc])).getSource ();
    return huffmanSources [codeNumber] [acdc];
  }

  Writeable getHuffmanSink (int codeNumber,int acdc)
  {
    if (huffmanSinks [codeNumber] [acdc] == null &&
        huffmanCodes [codeNumber] [acdc] != null)
      huffmanSinks [codeNumber] [acdc] = (Writeable) Concatenator.concatenate (new HuffmanEncoder (huffmanCodes [codeNumber] [acdc]),bitsToBytes).getSink ();
    return huffmanSinks [codeNumber] [acdc];
  }

  void readHuffmanCodes (InputStream in) throws IOException
  {
    int b;
    while ((b = in.read ()) != -1)
      {
        int codeNumber = b & 0xf;
        Assertions.limit (codeNumber,0,3);
        int acdc = (b & 0x10) >> 4;
        Assertions.expect (b >> 5,0);
        huffmanCodes [codeNumber] [acdc] = new HuffmanCode (in,huffmanLength);
        if (huffmanSources != null)
          huffmanSources [codeNumber] [acdc] = null;
        if (huffmanSinks != null)
          huffmanSinks [codeNumber] [acdc] = null;
      }
  }

  void readQuantizationTables (InputStream in) throws IOException
  {
    int b;
    while ((b = in.read ()) != -1)
      quantizationTables [b & 0xf] = new QuantizationTable (in,(b & 0xf0) != 0);
  }

  void readHuffmanCodes (String name) throws IOException {
    InputStream in = Resources.getInputStream (TableSpecification.class,name + ".dht");
    try { readHuffmanCodes (in); }
    finally { in.close (); }
  }

  void readQuantizationTables (String name) throws IOException {
    InputStream in = Resources.getInputStream (TableSpecification.class,name + ".dqt");
    try { readQuantizationTables (in); }
    finally { in.close (); }
  }
  
  void defaultCodes () throws IOException
  {
    huffmanCodes = new HuffmanCode [4] [2];
    readHuffmanCodes ("default");
  }

  void defaultTables () throws IOException
  {
    quantizationTables = new QuantizationTable [4];
    readQuantizationTables ("default");
  }

  void quantizationTables (float lossiness) throws IOException
  {
    defaultTables ();
    for (int i = 0;i < quantizationTables.length;i++)
      if (quantizationTables [i] != null)
        quantizationTables [i].scale (lossiness);
  }
}
