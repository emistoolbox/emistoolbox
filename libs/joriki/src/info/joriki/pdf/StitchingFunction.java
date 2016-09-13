/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.util.Vector;

import info.joriki.util.Assertions;

public class StitchingFunction extends PDFFunction
{
  PDFFunction [] functions;
  float [] bounds;
  float [] a;
  float [] b;
  int k;

  public StitchingFunction (float [] domain,
                            float [] range,
                            PDFFunction [] functions,
                            float [] bounds,
                            float [] encode)
  {
    super (domain,range,functions [0].n);
    this.functions = functions;
    this.bounds = bounds;
    k = functions.length;
    a = new float [k];
    b = new float [k];

    for (int i = 0,j = 0;i < k;i++,j += 2)
      {
        float x0 = i == 0 ? domain [0] : bounds [i-1];
        float x1 = i + 1 == k ? domain [1] : bounds [i];
        float y0 = encode [j];
        float y1 = encode [j+1];

        if (x0 == x1)
          {
            a [i] = 0;
            b [i] = y0;
          }
        else
          {
            a [i] = (y1 - y0) / (x1 - x0);
            b [i] = y0 - a [i] * x0;
          }
      }

    if (k > 1)
      {
        // All these assertions should have < instead of <=
        // according to the spec, but bt0762725923.pdf has
        // a case with
        // /Domain [ 0 1 ] and
        // /Bounds [ 0.00562 0.00562 1 1 ]. 
        // The spec talks about the case bounds [k - 2] == domain [1],
        // contradicting itself.
        Assertions.expect (domain [0] <= bounds [0] &&
                           bounds [k - 2] <= domain [1]);
        for (int i = 0;i < k - 2;i++)
          Assertions.expect (bounds [i] <= bounds [i+1]);
      }

    Assertions.expect (m,1);
    Assertions.expect (bounds.length,k - 1);
    Assertions.expect (encode.length,2 * k);

    for (int i = 0;i < functions.length;i++)
      Assertions.expect (functions [i].n,n);
  }

  private final float encode (float x,int i)
  {
    return a [i] * x + b [i];
  }

  private final float decode (float y,int i)
  {
    return (y - b [i]) / a [i];
  }

  private int intervalContaining (float x)
  {
    int i = 0;
    while (i < bounds.length && x >= bounds [i])
      i++;
    return i;
  }

  protected float [] valueFor (float [] x)
  {
    float x0 = x [0];
    int i = intervalContaining (x0);
    return functions [i].f (new float [] {encode (x0,i)});
  }

  protected void addClippedSamples (Vector sampleVector,
                                    float xa,float xb,
                                    float [] range,
                                    double maximalDeviation)
  {
    int ia = intervalContaining (xa);
    int ib = intervalContaining (xb);
    
    // remove the sample at the beginning of the interval,
    // since this will be duplicated by functions [0]
    sampleVector.removeElementAt (sampleVector.size () - 1);
    for (int i = ia;i <= ib;i++)
      {
        float x0 = i == ia ? xa : bounds [i-1];
        float x1 = i == ib ? xb : bounds [i];

        int first = sampleVector.size ();
        if (a [i] == 0) // no slope, just add end values
          {
            functions [i].addSample (sampleVector,range,b [i]);
            functions [i].addSample (sampleVector,range,b [i]);
          }
        else
          functions [i].addSamples (sampleVector,encode (x0,i),encode (x1,i),
                                    range,maximalDeviation);
        int last = sampleVector.size () - 1;

        for (int j = first;j <= last;j++)
          {
            Sample sample = (Sample) sampleVector.elementAt (j);
            // these could be decoded too, but there would be
            // rounding errors which could lead to swapping of
            // two sides of a discontinuity.
            if (j == first)
              sample.x = x0;
            else if (j == last)
              sample.x = x1;
            else
              sample.x = decode (sample.x,i);
          }
      }
    // remove the last sample added by the last function,
    // since this will be duplicated by the end of the interval
    sampleVector.removeElementAt (sampleVector.size () - 1);
  }
}
