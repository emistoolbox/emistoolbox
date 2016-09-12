/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.io.IOException;

import java.util.Vector;

import info.joriki.util.General;
import info.joriki.util.Assertions;
import info.joriki.util.NotImplementedException;

public class SampledFunction extends PDFFunction
{
  int [] size;
  float [] a;
  float [] b;
  Object [] samples;
  float [] [] results;
  float [] tmp;

  public SampledFunction
    (float [] domain,
     float [] range,
     PDFStream stream)
  {
    super (domain,range);
    
    size = stream.getIntArray ("Size");
    Assertions.expect (size.length,m);

    results = new float [m] [n];
    tmp = new float [n];

    int order = stream.getInt ("Order",1);
    if (order == 3)
      throw new NotImplementedException ("cubic interpolation");
    if (order != 1)
      throw new IllegalArgumentException ("Illegal interpolation order " + order);
    float [] encode = stream.getFloatArray ("Encode");
    if (encode == null)
      {
        encode = new float [2*m];
        for (int j = 0,i = 0;j < encode.length;j += 2,i++)
          {
            encode [j] = 0;
            encode [j+1] = size [i] - 1;
          }
      }
    Assertions.expect (encode.length,2*m);
    
    a = new float [m];
    b = new float [m];

    for (int i = 0,j = 0;i < m;i++,j += 2)
      {
        float x0 = domain [j];
        float x1 = domain [j+1];
        float y0 = encode [j];
        float y1 = encode [j+1];
        a [i] = (y1 - y0) / (x1 - x0);
        b [i] = y0 - a [i] * x0;
      }

    try {
      samples = new SampleReader (stream).readSamples ();
    } catch (IOException ioe) {
      ioe.printStackTrace();
      throw new Error ("couldn't read sample data");
    }
  }
  
  /*
    protected float [] valueFor (float [] x)
    {
    float s = Math.max (0,Math.min (size [0] - 1,a [0] * x [0] + b [0]));
    int i = (int) s;
    s -= i;
    float [] lower = samples [i];
    if (s == 0) // exactly on sample, just copy
    System.arraycopy (lower,0,result,0,n);
    else
    {
    float [] upper = samples [i+1];
    float t = 1 - s;
    for (int k = 0;k < n;k++)
    result [k] = s * upper [k] + t * lower [k];
    }
    return result;
    }
  */

  protected float [] valueFor (float [] x)
  {
    // results [0] is never used, since the result for j == 0
    // is a sample value. Hence we can use it here for the final result.
    return valueFor (x,samples,m,results [0]);
  }

  protected float [] valueFor (float [] x,Object array,int j,float [] result)
  {
    if (j == 0)
      return (float []) array;
    Object [] arrays = (Object []) array; 
    j--;
    float s = General.clip (a [j] * x [j] + b [j],0,size [j] - 1);
    int i = (int) s;
    s -= i;
    float [] lower = valueFor (x,arrays [i],j,results [j]);
    if (s == 0) // exactly on sample, just copy
      System.arraycopy (lower,0,result,0,n);
    else
      {
        // this value isn't stored across other calls to
        // valueFor; hence we can use the same tmp array
        // each time.
        float [] upper = valueFor (x,arrays [i+1],j,tmp);
        float t = 1 - s;
        for (int k = 0;k < n;k++)
          result [k] = s * upper [k] + t * lower [k];
      }
    return result;
  }

  protected void addClippedSamples (Vector sampleVector,
                                    float xa,float xb,
                                    float [] innerRange,
                                    double maximalDeviation)
  {
    Assertions.expect (m,1);
    float [] [] samples = (float [] []) this.samples;
    // first the range intersections
    for (int i = 0;i < size [0] - 1;i++)
      {
        float [] lower = samples [i];
        float [] upper = samples [i + 1];
        for (int j = 0;j < innerRange.length;j++)
          {
            int k = j >> 1;
            float r = innerRange [j];
            // I had <= here all along -- weird this didn't cause more problems
            if ((lower [k] - r) * (r - upper [k]) > 0) // == is covered by normal samples
              // the kth component takes the value r in this interval
              {
                float s = (r - lower [k]) / (upper [k] - lower [k]);
                addSample (sampleVector,innerRange,((i + s) - b [0]) / a [0]);
              }
          }
      }
    // now the normal samples
    for (int i = 1;i < size [0] - 1;i++)
      sampleVector.addElement
        (new Sample ((i - b [0]) / a [0],samples [i].clone ()));
  }
}
