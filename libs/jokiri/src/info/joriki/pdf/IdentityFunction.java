/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.util.Vector;

public class IdentityFunction extends PDFFunction
{
  public IdentityFunction (float [] domain)
  {
    super (domain,null,domain.length / 2);
  }

  public IdentityFunction (float [] domain,float [] range)
  {
    super (domain,range);
  }

  public float [] valueFor (float [] x)
  {
    return x.clone ();
  }

  protected void addClippedSamples (Vector sampleVector,
                                    float xa,float xb,
                                    float [] range,
                                    double maximalDeviation)
  {
    if (range != null)
      for (int i = 0;i < range.length;i++)
        if (xa < range [i] && range [i] < xb)
          addSample (sampleVector,null,range [i]);
  }
}  
