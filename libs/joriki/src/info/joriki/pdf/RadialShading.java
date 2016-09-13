/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import info.joriki.util.Assertions;

import info.joriki.graphics.Point;
import info.joriki.graphics.Transformation;

public class RadialShading extends LinearShading
{
  // first index which end, second index x,y,r
  public double [] [] circle = new double [2] [3];
  // center and radius separated for convenience
  public double [] [] center = new double [2] [2];
  public double [] radius = new double [2];

  RadialShading (PDFDictionary dictionary,ResourceResolver resourceResolver)
  {
    super (dictionary,resourceResolver);

    double [] coords = dictionary.getDoubleArray ("Coords");
    Assertions.expect (coords.length,6);
    for (int i = 0,k = 0;i < 2;i++)
    {
      for (int j = 0;j < 3;j++,k++)
        circle [i] [j] = coords [k];
      center [i] [0] = circle [i] [0];
      center [i] [1] = circle [i] [1];
      radius [i]     = circle [i] [2];
      
      Assertions.expect (radius [i] >= 0);
    }
    dictionary.checkUnused ("4.28");
  }

  final double [] point = new double [3];

  double getParameter (Point p)
  {
    double a = 0;
    double b = 0;
    double c = 0;
    point [0] = p.x;
    point [1] = p.y;
    for (int j = 0;j < 3;j++)
    {
      double u = circle [0] [j] - point [j];
      double v = circle [1] [j] - circle [0] [j];
      double sign = j == 2 ? -1 : 1;
      a += sign * v * v;
      b += 2 * sign * u * v;
      c += sign * u * u;
    }
    
    double rad = b * b - 4 * a * c;
    if (rad >= 0)
    {
      rad = Math.sqrt (rad);
      for (int sign = 1;sign >= -1;sign -= 2)
      {
        double s = (-b + sign * rad) / (2 * a);
        if (radius [0] + s * (radius [1] - radius [0]) >= 0)
        {
          if (s > 1)
          {
            if (extend [1])
              return 1;
          }
          else if (s < 0)
          {
            if (extend [0])
              return 0;
          }
          else
            return s;
        }
      }
    }
    return TRANSPARENT;
  }

  final static double TRANSPARENT = -1;

  protected void rasterize (Transformation transform,Subsampler subsampler)
  {
    final Transformation inverse = transform.inverse ();
    subsampler.traverse (new Shader () {
      public float [] shade (Point p)
      {
        p.transformBy (inverse);
        double s = getParameter (p);
        return s == TRANSPARENT ? null : new float []
        {domain [0] + ((float) s) * (domain [1] - domain [0])};
      }
    });
  }

  public boolean overwrites ()
  {
    double sum = 0;
    for (int j = 0;j < 3;j++)
    {
      double d = circle [1] [j] - circle [0] [j];
      sum += (j == 2 ? -1 : 1) * d * d;
    }
    return sum > 0;
  }
}
