/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.util.Vector;
import java.util.Arrays;

import info.joriki.util.General;
import info.joriki.util.Assertions;
import info.joriki.util.NotImplementedException;

abstract public class PDFFunction
{
  final static int SAMPLED = 0;
  final static int EXPONENTIAL = 2;
  final static int STITCHING = 3;
  final static int CALCULATOR = 4;

  protected float [] domain;
  protected float [] range;
  protected int m,n;
  
  protected PDFFunction (float [] domain,float [] range)
  {
    this (domain,range,range.length / 2);
  }

  protected PDFFunction (float [] domain,float [] range,int n)
  {
    this.domain = domain;
    this.range  = range;
    this.m = domain.length / 2;
    this.n = n;

    checkIntervals (domain,m);
    if (range != null)
      checkIntervals (range,n);
  }
  
  private void checkIntervals (float [] intervals,int dimension)
  {
    Assertions.expect (intervals.length,2 * dimension);
    for (int j = 0;j < intervals.length;j += 2)
      Assertions.expect (intervals [j] <= intervals [j+1]);
  }

  private void clip (float [] vals,float [] intervals)
  {
    Assertions.expect (intervals.length,2 * vals.length);
    for (int i = 0,j = 0;i < vals.length;i++,j += 2)
      vals [i] = General.clip (vals [i],intervals [j],intervals [j+1]);
  }

  public float [] f (float [] x)
  {
    Assertions.expect (x.length,m);
    clip (x,domain);
    float [] result = valueFor (x);
    if (range != null)
      clip (result,range);
    return result;
  }

  abstract protected float [] valueFor (float [] x);

  public Sample [] getSamples (float xa,float xb,
                               double maximalDeviation,
                               float [] outerRange)
  {
    Assertions.expect (m,1);
    Vector sampleVector = new Vector ();
    addSamples
      (sampleVector,xa,xb,outerRange,maximalDeviation);
      
    Sample [] samples = new Sample [sampleVector.size ()];
    sampleVector.copyInto (samples);
    // if this is reimplemented, make sure it remains stable,
    // that is, two equal elements musn't be rearranged.
    Arrays.sort (samples);

    int nsample = 1;
    for (int i = 1;i < samples.length;i++)
      if (!samples [i].equals (samples [nsample-1]))
        samples [nsample++] = samples [i];

    if (nsample == samples.length)
      return samples;

    Sample [] uniqueSamples = new Sample [nsample];
    System.arraycopy (samples,0,uniqueSamples,0,nsample);
    
    return uniqueSamples;
  }

  protected void addSample (Vector sampleVector,float [] range,float x)
  {
    Assertions.expect (m,1);
    float [] fx = f (new float [] {x}).clone ();
    if (range != null)
      clip (fx,range);
    sampleVector.addElement (new Sample (x,fx));
  }

  protected void addSamples (Vector sampleVector,
                             float xa,float xb,
                             float [] outerRange,
                             double maximalDeviation)
  {
    Assertions.expect (m,1);

    float ca = General.clip (xa,domain [0],domain [1]);
    float cb = General.clip (xb,domain [0],domain [1]);

    addSample (sampleVector,outerRange,xa);
    if (ca != xa)
      addSample (sampleVector,outerRange,ca);
    addClippedSamples
      (sampleVector,ca,cb,concatenation (outerRange,range),maximalDeviation);
    if (cb != xb)
      addSample (sampleVector,outerRange,cb);
    addSample (sampleVector,outerRange,xb);
  }

  abstract protected void addClippedSamples (Vector sampleVector,
                                             float xa,float xb,
                                             float [] innerRange,
                                             double maximalDeviation);

  // clipping resulting from first clipping to inner, then to outer range
  static float [] concatenation (float [] outerRange,float [] innerRange)
  {
    Assertions.expect (outerRange == null || innerRange == null || outerRange.length == innerRange.length);

    if (outerRange == null)
      return innerRange;
    if (innerRange == null)
      return outerRange;

    float [] concatenation = new float [innerRange.length];
    for (int i = 0;i < concatenation.length;i++)
      {
        int j = i & ~1;
        concatenation [i] = General.clip
          (innerRange [i],outerRange [j],outerRange [j+1]);
      }
    return concatenation;
  }

  public static PDFFunction getInstance
    (PDFObject specification,ResourceResolver resourceResolver)
  {
    if (specification instanceof PDFName)
      {
        if (((PDFName) specification).getName ().equals ("Identity"))
          return new IdentityFunction (new float [] {0,1});
        throw new NotImplementedException ("Named function " + specification);
      }

    PDFDictionary dictionary = (PDFDictionary) specification;
    
    float [] domain = dictionary.getFloatArray ("Domain");
    float [] range  = dictionary.getFloatArray ("Range");

    int functionType = dictionary.getInt ("FunctionType");

    switch (functionType)
      {
      case SAMPLED :
        return new SampledFunction (domain,range,(PDFStream) dictionary);
      case EXPONENTIAL :
        double exponent = dictionary.getDouble ("N");
        PDFArray c0 = (PDFArray) dictionary.get ("C0");
        PDFArray c1 = (PDFArray) dictionary.get ("C1");
        dictionary.checkUnused ("3.36");
        return new ExponentialFunction
          (domain,range,exponent,
           c0 != null ? c0.toFloatArray () : new float [] {0},
           c1 != null ? c1.toFloatArray () : new float [] {1});
      case STITCHING :
        PDFArray functionArray = (PDFArray) dictionary.get ("Functions");
        PDFFunction [] functions = new PDFFunction [functionArray.size ()];
        for (int i = 0;i < functions.length;i++)
          functions [i] = (PDFFunction) resourceResolver.getCachedObject
            (ObjectTypes.FUNCTION,functionArray.get (i));
      
        float [] bounds = dictionary.getFloatArray ("Bounds");
        float [] encode = dictionary.getFloatArray ("Encode");
        dictionary.checkUnused ("3.37");
        return new StitchingFunction (domain,range,functions,bounds,encode);
      case CALCULATOR :
        return new CalculatorFunction (domain,range,(PDFStream) dictionary);
      default :
        throw new NotImplementedException ("function type " + functionType);
      }
  }

  // takes multi-dimensional samples for an array of one-dimensional functions
  public static Sample [] getSamples (PDFFunction [] functions,
                                      float xa,float xb,
                                      double maximalDeviation,
                                      float [] outerRange)
  {
    int n = functions.length;

    for (int i = 0;i < n;i++)
      {
        PDFFunction function = functions [i];
        Assertions.expect (function.m,1);
        Assertions.expect (function.n,1);
      }

    Sample [] [] samples = new Sample [n] [];

    for (int i = 0;i < n;i++)
      samples [i] = functions [i].getSamples
        (xa,xb,maximalDeviation,new float [] {outerRange [2*i],
                                              outerRange [2*i+1]});

    // go through from the beginning. In each step, take the smallest
    // remaining x value, then use the values for all functions whose next
    // sample is at x, and evaluate all others. This is designed to work
    // for the case when one or more of the functions have discontinuities
    // with two consecutive samples with the same x value.

    Vector sampleVector = new Vector ();
    float x = xa;

    int [] index = new int [n];
    
    do
      {
        float [] xarr = {x};
        Sample sample = new Sample (x,new float [n]);
        float nextx = Float.MAX_VALUE;
        for (int i = 0;i < n;i++)
          {
            if (index [i] == samples [i].length)
              continue;
            Sample samplei = samples [i] [index [i]];
            if (samplei.x == x)
              {
                sample.y [i] = samplei.y [0];
                if (++index [i] == samples [i].length)
                  continue;
              }
            else
              sample.y [i] = functions [i].f (xarr) [0];
            nextx = Math.min (nextx,samples [i] [index [i]].x);
          }
        sampleVector.addElement (sample);
        x = nextx;
      }
    while (x != Float.MAX_VALUE);
      
    Sample [] result = new Sample [sampleVector.size ()];
    sampleVector.copyInto (result);
    
    return result;
  }

  public boolean isDefinedOn (float [] cuboid)
  {
    Assertions.expect (cuboid.length,domain.length);

    for (int j = 0;j < domain.length;j += 2)
      if (cuboid [j] < domain [j] || cuboid [j+1] > domain [j+1])
        return false;

    return true;
  }
}
