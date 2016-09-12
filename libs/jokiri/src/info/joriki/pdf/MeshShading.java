/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.io.IOException;

import java.util.List;
import java.util.ArrayList;

import info.joriki.io.InputStreamBitSource;

import info.joriki.graphics.Rectangle;
import info.joriki.graphics.Transformation;

abstract public class MeshShading extends PDFShading
{
  int bitsPerFlag; // only used by free forms
  int bitsPerComponent;
  int bitsPerCoordinate;

  List points = new ArrayList ();

  float [] decode;

  private InputStreamBitSource source;

  public MeshShading (PDFDictionary dictionary,ResourceResolver resourceResolver,String table)
  {
    super (dictionary,resourceResolver,1);
    
    bitsPerFlag = dictionary.getInt ("BitsPerFlag",0);
    bitsPerComponent = dictionary.getInt ("BitsPerComponent");
    bitsPerCoordinate = dictionary.getInt ("BitsPerCoordinate");
    decode = dictionary.getFloatArray ("Decode");
    if (dictionary.contains ("Function"))
      checkDomain (new float [] {decode [4],decode [5]});
    try {
      source = new InputStreamBitSource
        (((PDFStream) dictionary).getInputStream (table),false);
      readData ();
    } catch (IOException ioe) {
      ioe.printStackTrace ();
      throw new Error ("can't read shading mesh data");
    }
  }

  abstract protected void readData () throws IOException;

  private float decode (int bits,int index) throws IOException
  {
    index *= 2;
    float dmin = decode [index];
    float dmax = decode [index+1];
    long b;
    if (bits == 32) // can't read 32 bits at once from BitBuffer
      {
        b = source.readBits (16);
        b <<= 16;
        b |= source.readBits (16);
      }
    else
      b = source.readBits (bits);
    return dmin + b * (dmax - dmin) / ((1L << bits) - 1);
  }

  protected boolean moreData () throws IOException
  {
    source.byteAlign ();
    return source.peekBits (1) >= 0;
  }
  
  protected int readFlag () throws IOException
  {
    return source.readBits (bitsPerFlag);
  }

  protected float [] readColor () throws IOException
  {
    float [] color = new float [ncomponents];
    for (int i = 0;i < color.length;i++)
      color [i] = decode (bitsPerComponent,i + 2);
    return color;
  }

  protected float [] readPoint () throws IOException
  {
    float [] point = new float [2];
    for (int i = 0;i < 2;i++)
      point [i] = decode (bitsPerCoordinate,i);
    points.add (point);
    return point;
  }

  public Rectangle getBoundingBox (Transformation transform)
  {
    Rectangle boundingBox = new Rectangle ();
    for (int i = 0;i < points.size ();i++)
      {
        float [] point = (float []) points.get (i);
        double [] dpoint = new double [] {point [0],point [1]};
        transform.transform (dpoint);
        boundingBox.add (dpoint);
      }
    return boundingBox;
  }
}
