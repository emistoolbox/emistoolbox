/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.font;

import java.util.Iterator;
import java.util.Collection;

import info.joriki.util.MultiSet;

public class Widths
{
  public double defaultWidth;
  public double nominalWidth;

  public Widths (double defaultWidth,double nominalWidth)
  {
    this.defaultWidth = defaultWidth;
    this.nominalWidth = nominalWidth;
  }
  
  public Widths (Collection widths)
  {
    defaultWidth =
      ((Number) new MultiSet (widths).mostFrequentElement ()).doubleValue ();
    int n = 0;
    double sum = 0;
    Iterator iterator = widths.iterator ();
    while (iterator.hasNext ())
      {
        double width = ((Number) iterator.next ()).doubleValue ();
        if (width != defaultWidth)
          {
            n++;
            sum += width;
          }
      }
    nominalWidth = sum / n;
  }
}
