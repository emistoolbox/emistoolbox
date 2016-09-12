/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.util.Vector;

import info.joriki.util.Assertions;

public class ExponentialFunction extends PDFFunction
{
  double exponent;
  float [] c0;
  float [] c1;
  float [] result;

  public ExponentialFunction (float [] domain,float [] range,double exponent,
                              float [] c0,float [] c1)
  {
    super (domain,range,c0.length);
    this.exponent = exponent;
    this.c0 = c0;
    this.c1 = c1;

    result = new float [n];

    for (int i = 0;i < n;i++)
      c1 [i] -= c0 [i];

    Assertions.expect (m,1);
    Assertions.expect (c0.length,n);
    Assertions.expect (c1.length,n);

    Assertions.expect (!((exponent != (int) exponent) && domain [0] < 0));
    Assertions.expect (!(exponent < 0 && domain [0] * domain [1] < 0));
  }

  protected float [] valueFor (float [] x)
  {
    float pow = (float) Math.pow (x [0],exponent);
    for (int i = 0;i < n;i++)
      result [i] = c0 [i] + c1 [i] * pow;
    return result;
  }

  protected void addClippedSamples (Vector sampleVector,
                                    float xa,float xb,
                                    float [] range,
                                    double maximalDeviation)
  {
    // check for range clipping
    // whenever we find that one of the functions takes a limit value,
    // we split the interval at that point and add it to the list
    if (range != null && exponent != 0)
      {
        double root = 1 / exponent;
        for (int i = 0,j = 0;i < n;i++,j += 2)
          if (c1 [i] != 0)
            for (int l = 0;l < 2;l++)
              {
                float xr = (float) Math.pow ((range [j+l] - c0 [i]) / c1 [i],root);
                // if we already clipped for this component,
                // one of these comparisons will be ==
                if (xa < xr && xr < xb)
                  {
                    recurse (sampleVector,xa,xr,xb,range,maximalDeviation);
                    return;
                  }
              }
      }

    // now recursively split the interval at the worst deviation point
    // until the deviation is acceptable.
    if (exponent != 0 && exponent != 1) // these two have zero error
      {
        double deviation = 0;
        double xdev = 0;
        double pa = Math.pow (xa,exponent);
        double pb = Math.pow (xb,exponent);

        for (int i = 0;i < n;i++)
          {
            if (c1 [i] == 0)
              continue; // again, zero error
            // function evaluations are duplicated;
            // would be more efficient further out in the
            // recursion, but more complicated to do.
            double ya = c0 [i] + c1 [i] * pa;
            double yb = c0 [i] + c1 [i] * pb;
            double slope = (yb - ya) / (xb - xa);
            double arg = slope / (exponent * c1 [i]);
            if (arg <= 0)
              continue; // no extremum in the deviation
            double xextr = Math.pow (arg,1 / (exponent - 1));
            if ((xa - xextr) * (xextr - xb) <= 0)
              continue; // extremum outside the interval
            double yextr = c0 [i] + c1 [i] * Math.pow (xextr,exponent);
            // make sure it hasn't already been clipped
            if (range != null && 
                !(range [2*i] < yextr && yextr < range [2*i+1]))
              continue; // has already been clipped
            double intercept = ya - slope * xa;
            double del = Math.abs (yextr - (intercept + slope * xextr));
            if (del > deviation)
              {
                deviation = del;
                xdev = xextr;
              }
          }

        if (deviation > maximalDeviation)
          // range has been taken care of
          recurse (sampleVector,xa,(float) xdev,xb,null,maximalDeviation);
      }
  }

  private void recurse (Vector sampleVector,
                        float xa,float xz,float xb,
                        float [] range,
                        double maximalDeviation)
  { 
    addSample (sampleVector,range,xz);
    addClippedSamples (sampleVector,xa,xz,range,maximalDeviation);
    addClippedSamples (sampleVector,xz,xb,range,maximalDeviation);
  }

}
