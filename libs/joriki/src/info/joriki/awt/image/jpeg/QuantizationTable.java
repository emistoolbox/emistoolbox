/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.jpeg;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import info.joriki.awt.image.CartesianTransform;

import info.joriki.util.General;

class QuantizationTable implements JPEGSpeaker
{
  final static float [] factors = new float [DCTsize];
  final static float sqrt2 = (float) Math.sqrt (2);
  static float factor (int k)
  {
    return k == 0 ? 1 : sqrt2 * (float) Math.cos ((Math.PI/(2*DCTlength)) * k);
  }

  static {
    for (int kx = 0;kx < DCTlength;kx++)
      for (int ky = 0;ky < DCTlength;ky++)
        factors [ky * DCTlength + kx] = factor (kx) * factor (ky);
  }

  short [] rawData = new short [DCTsize]; // in zig zag order
  float [] table = new float [DCTsize];   // in cartesian matrix order

  private QuantizationTable () {}
  
  QuantizationTable (InputStream in,boolean twoByte) throws IOException
  {
    DataInputStream dis = new DataInputStream (in);

    for (int i = 0;i < table.length;i++)
      rawData [i] = twoByte ? dis.readShort () : (short) dis.readUnsignedByte ();
    fillTable ();
  }

  void fillTable ()
  {
    for (int i = 0;i < table.length;i++)
      {
        int index = ZigZag.zigZag [i];
        table [index] = factors [index] * rawData [i];
      }
  }

  boolean isTwoByte ()
  {
    for (int i = 0;i < table.length;i++)
      if (rawData [i] > 255)
        return true;
    return false;
  }

  void writeTo (OutputStream out,boolean twoByte) throws IOException
  {
    DataOutputStream dos = new DataOutputStream (out);

    for (int i = 0;i < table.length;i++)
      if (twoByte)
        dos.writeShort (rawData [i]);
      else
        dos.writeByte (rawData [i]);
  }

  void dequantize (short [] src,float [] dest)
  {
    for (int i = 0;i < DCTsize;i++)
      dest [i] = table [i] * src [i];
  }

  void quantize (float [] src,short [] dest)
  {
    for (int i = 0;i < DCTsize;i++)
      dest [i] = (short) Math.round (src [i] / table [i]);
  }

  void scale (float lossiness)
  {
    for (int i = 0;i < table.length;i++)
      rawData [i] = (short) General.clip (Math.round (rawData [i] * lossiness),1,255);
    fillTable ();
  }

  QuantizationTable transformedBy (CartesianTransform transform)
  {
    QuantizationTable transformed = new QuantizationTable ();
    transformed.rawData = rawData.clone ();
    transformed.table = table.clone ();
    if (transform.swaps ())
      {
        int [] zagZig = ZigZag.zagZig;
        for (int y = 0;y < DCTlength;y++)
          for (int x = 0;x < DCTlength;x++)
            {
              int index = y * DCTlength + x;
              int swappedIndex = x * DCTlength + y;

              transformed.table [index] = table [swappedIndex];
              transformed.rawData [zagZig [index]] =
                rawData [zagZig [swappedIndex]];
            }
      }

    return transformed;
  }

  private int sum ()
  {
    int sum = 0;
    for (int i = 0;i < rawData.length;i++)
      sum += rawData [i];
    return sum;
  }

  public boolean finerThan (QuantizationTable table)
  {
    return sum () < table.sum ();
  }

  public String toString ()
  {
    StringBuilder stringBuilder = new StringBuilder ();

    for (int y = 0,i = 0;y < DCTlength;y++)
      {
        for (int x = 0;x < DCTlength;x++,i++)
          {
            stringBuilder.append (rawData [ZigZag.zagZig [i]]);
            stringBuilder.append (' ');
          }
        stringBuilder.append ('\n');
      }

    return stringBuilder.toString ();
  }
}
