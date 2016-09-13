/*
 * Copyright 2005 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import info.joriki.io.NybbleInputStream;
import info.joriki.util.NotImplementedException;

import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;

public class SampleReader {
  InputStream in;
  int [] size;
  float [] decode;
  int readsPerSample;
  float maxSample;
  
  public SampleReader (PDFStream stream) {
    int bitsPerSample = stream.getInt ("BitsPerSample");
    boolean nybbles = 8 % bitsPerSample == 0;
    boolean bytes = bitsPerSample % 8 == 0;
    readsPerSample = nybbles ? 1 : bitsPerSample / 8;
    maxSample = (1L << bitsPerSample) - 1;
    size = stream.getIntArray ("Size");
    decode = stream.getFloatArray ("Decode",stream.getFloatArray ("Range"));

    if (bitsPerSample == 12)
      throw new NotImplementedException ("12-bit samples");
    if (!(nybbles || (bytes && (bitsPerSample <= 32))))
      throw new IllegalArgumentException ("Illegal sample bit length " + bitsPerSample);
    try {
      in = stream.getInputStream ("3.35");
      if (bitsPerSample < 8)
        in = new NybbleInputStream (in,bitsPerSample,false);
    } catch (IOException ioe) {
      ioe.printStackTrace ();
      throw new Error ("couldn't read sample data");
    }
  }

  private float [] [] readRawSamples (int n) throws IOException {
    float [] [] samples = new float [n] [decode.length / 2];

    for (int i = 0;i < samples.length;i++)
      for (int k = 0,j = 0;j < decode.length;k++,j += 2)
        {
          int sample = 0;
          for (int l = 0;l < readsPerSample;l++)
            {
              sample <<= 8;
              int read = in.read ();
              if (read < 0)
                throw new StreamCorruptedException ("Unexpected EOF on sample data");
              sample |= read;
            }
          samples [i] [k] = decode [j] +
            (decode [j+1] - decode [j]) * (sample & 0xffffffffL) / maxSample;
        }

    return samples;
  }
  
  public float [] [] readRawSamples () throws IOException {
    int nsamples = 1;
    for (int i = 0;i < size.length;i++)
      nsamples *= size [i];
    return readRawSamples (nsamples);
  }
  
  private Object [] readSamples (int d) throws IOException
  {
    if (--d == 0)
      return readRawSamples (size [0]);
    Object [] result = new Object [size [d]];
    for (int i = 0;i < result.length;i++)
      result [i] = readSamples (d);
    return result;
  }
  
  public Object [] readSamples () throws IOException {
    return readSamples (size.length);
  }  
 }
