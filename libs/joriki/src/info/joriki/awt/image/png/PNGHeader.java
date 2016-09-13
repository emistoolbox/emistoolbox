/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.png;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class PNGHeader
{
  public int width;
  public int height;
  public int bitDepth;
  public byte colorType;
  public byte compressionMethod;
  public byte filterMethod;
  public byte interlaceMethod;

  PNGHeader () {}

  PNGHeader (byte [] data) throws IOException
  {
    this (new ByteArrayInputStream (data));
  }
    
  PNGHeader (InputStream in) throws IOException
  {
    this (new DataInputStream (in));
  }

  PNGHeader (DataInputStream dis) throws IOException
  {
    readFrom (dis);
  }

  void readFrom (DataInputStream dis) throws IOException
  {
    width = dis.readInt ();
    height = dis.readInt ();
    bitDepth = dis.read ();
    colorType = (byte) dis.read ();
    compressionMethod = (byte) dis.read ();
    filterMethod = (byte) dis.read ();
    interlaceMethod = (byte) dis.read ();
  }

  void writeTo (DataOutputStream dos) throws IOException
  {
    dos.writeInt (width);
    dos.writeInt (height);
    dos.write (bitDepth);
    dos.write (colorType);
    dos.write (compressionMethod);
    dos.write (filterMethod);
    dos.write (interlaceMethod);
  }
}
