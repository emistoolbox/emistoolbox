/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.io.IOException;

import java.util.List;
import java.util.ArrayList;

import info.joriki.util.General;
import info.joriki.util.Assertions;

import info.joriki.graphics.Transformation;

public class PatchShading extends MeshShading
{
  public class Patch
  {
    float [] [] [] points = new float [4] [4] [];
    float [] [] [] colors = new float [2] [2] [];
    float [] [] [] coeffs = new float [4] [4] [];

    void computeCoefficients ()
    {
      for (int i = 0;i < 4;i++)
        for (int j = 0;j < 4;j++)
          {
            float [] sums = new float [2];
            for (int k = 0;k < 4;k++)
              for (int l = 0;l < 4;l++)
                {
                  float factor = transform [k] [i] * transform [l] [j] / 64;
                  float [] point = points [k] [l];
                  for (int x = 0;x < 2;x++)
                    sums [x] += factor * point [x];
                }
            coeffs [i] [j] = sums;
          }
    }

    // bicubic position interpolation over unit square
    double [] surface (float u,float v)
    {
      double [] result = new double [2];

      float s = 2 * u - 1;
      float t = 2 * v - 1;

      float si = 1;
      for (int i = 0;i < 4;i++)
        {
          float [] [] ci = coeffs [i];
          float tj = 1;
          for (int j = 0;j < 4;j++)
            {
              float [] cj = ci [j];
              float prod = si * tj;
              for (int x = 0;x < 2;x++)
                result [x] += prod * cj [x];
              tj *= t;
            }
          si *= s;
        }

      return result;
    }

    // bilinear color interpolation over unit square
    float [] color (float u,float v)
    {
      float [] color = new float [ncomponents];
      float [] us = new float [] {1-u,u};
      float [] vs = new float [] {1-v,v};
      for (int i = 0;i < 2;i++)
        for (int j = 0;j < 2;j++)
          for (int k = 0;k < ncomponents;k++)
            color [k] += us [i] * vs [j] * colors [i] [j] [k];
      return color;
    }

    float [] maximalDerivatives (Transformation transform)
    {
      Transformation linearPart = transform.linearPart ();
      // first index is x/y, second is u/v
      float [] [] d = new float [2] [2];
      for (int i = 0;i < 4;i++)
        for (int j = 0;j < 4;j++)
          {
            float [] coeff = coeffs [i] [j].clone ();
            linearPart.transform (coeff);
            for (int x = 0;x < 2;x++)
              {
                float abs = Math.abs (coeff [x]);
                d [x] [0] += i * abs;
                d [x] [1] += j * abs;
              }
          }
      float [] result = new float [2];
      for (int x = 0;x < 2;x++)
        result [x] = Math.max (d [0] [x],d [1] [x]);
      return result;
    }

    /* only for testing the other representation 
       void bernsteinSurface (float u,float v,float [] result)
       {
       float u1 = 1 - u;
       float [] us = new float [] {u1*u1*u1,3*u*u1*u1,3*u*u*u1,u*u*u};
       float v1 = 1 - v;
       float [] vs = new float [] {v1*v1*v1,3*v*v1*v1,3*v*v*v1,v*v*v};
       result [0] = result [1] = 0;
       for (int i = 0;i < 4;i++)
       for (int j = 0;j < 4;j++)
       for (int x = 0;x < 2;x++)
       result [x] += us [i] * vs [j] * points [i] [j] [x];
       }
    */
  }

  final static float [] [] transform = {
    {1,-3,3,-1},
    {3,-3,-3,3},
    {3,3,-3,-3},
    {1,3,3,1}
  };

  final static int [] [] tensorIndices = {
    {1,3},
    {2,3},
    {3,3},
    {3,2},
    {3,1},
    {3,0},
    {2,0},
    {1,0},
    {1,1},
    {1,2},
    {2,2},
    {2,1}
  };

  final static float [] coefficients = {-4,6,6,-2,-2,3,3,-1};
  static {
    for (int i = 0;i < coefficients.length;i++)
      coefficients [i] /= 9;
  }

  public List patches;

  PatchShading (PDFDictionary dictionary,ResourceResolver resourceResolver)
  {
    super (dictionary,resourceResolver,"4.31");
  }

  protected void readData () throws IOException
  {
    Assertions.unexpect (bitsPerFlag,0);

    patches = new ArrayList ();

    Patch previousPatch = null;

    while (moreData ())
      {
        Patch patch = new Patch ();
        int flag = readFlag ();
        Assertions.limit (flag,0,3);

        for (int i = 0;i < 4;i++)
          patch.points [0] [i] =
            flag == 0 ? readPoint () :
          flag == 1 ? previousPatch.points [i] [3] :
          flag == 2 ? previousPatch.points [3] [3-i] :
          flag == 3 ? previousPatch.points [3-i] [0] :
          null; // prevented by above assertion

        // for Coons patch, the internal control points
        // are not read from the data...

        for (int i = 0;i < (type == COONS_PATCH ? 8 : 12);i++)
          patch.points
            [tensorIndices [i] [0]]
            [tensorIndices [i] [1]]
            = readPoint ();

        // ..., they are computed from the external control points.
        if (type == COONS_PATCH)
          for (int i0 = 0,i1 = 1,i3 = 3;i1 <= 2;i0 += 3,i1++,i3 -= 3)
            for (int j0 = 0,j1 = 1,j3 = 3;j1 <= 2;j0 += 3,j1++,j3 -= 3)
              patch.points [i1] [j1] =
                General.linearCombination
                (coefficients,new float [] [] {
                  patch.points [i0] [j0],
                  patch.points [i0] [j1],
                  patch.points [i1] [j0],
                  patch.points [i0] [j3],
                  patch.points [i3] [j0],
                  patch.points [i3] [j1],
                  patch.points [i1] [j3],
                  patch.points [i3] [j3]
                });

        patch.computeCoefficients ();

        for (int i = 0;i < 2;i++)
          patch.colors [0] [i] =
          flag == 0 ? readColor () :
          flag == 1 ? previousPatch.colors [i] [1] :
          flag == 2 ? previousPatch.colors [1] [1-i] :
          flag == 3 ? previousPatch.colors [1-i] [0] :
          null; // prevented by above assertion

        patch.colors [1] [1] = readColor ();
        patch.colors [1] [0] = readColor ();

        patches.add (patch);
        previousPatch = patch;
      }
  }

  // 2 for scaling from [0,1] to [-1;1],
  // sqrt (2) for making sure we don't miss pixels diagonally
  final static float stepFactor = (float) Math.pow (2,1.5);

  protected void rasterize (Transformation transform,Subsampler subsampler)
  {
    for (int k = 0;k < patches.size ();k++)
      {
        Patch patch = (Patch) patches.get (k);

        float [] maximalDerivatives = patch.maximalDerivatives (transform);
        float factor = stepFactor * subsampler.nsub;
        int nu = (int) (factor * maximalDerivatives [0] + 1);
        int nv = (int) (factor * maximalDerivatives [1] + 1);
        float du = 1f / nu;
        float dv = 1f / nv;
        float u,v;
        int i,j;
        for (u = i = 0;i < nu;i++,u += du)
          for (v = j = 0;j < nv;j++,v += dv)
            {
              double [] surface = patch.surface (u,v);
              transform.transform (surface);
              subsampler.sample (surface,patch.color (u,v));
            }
      }
  }
}

/* stuff on how to make Coons patches a special case of tensor product patches

The 12 Coons control points are labeled as follows:

A AC CA C
AB     CD
BA     DC
B BD DB D

We expand the Coons surface in products of Bernstein
polynomials (see the PDF spec). By writing e.g (1 - u)
as (1 - u) (1 - u + u)^2
=  (1 - u) (1 - u)^2 + 2 (1 - u) (1 - u) u + (1 - u) u^2
=  (1 - u)^3 + 2 (1 - u)^2 u + (1 - u) u^2
we can determine how the Coons control points contribute
to the coefficients of these products.
The entries in the following squares correspond to the
products of the Bernstein polynomials, but with the factor
of 3 in B1 and B2 missing.
Labels on a row or column show which control point
is making the contributions in that row or column.

contribution from vertical linear interpolation:
                 A AC CA C
    1 2 1 0 A    1 3  3  1
    3 6 3 0 AB   2 6  6  2
    3 6 3 0 BA   1 3  3  1
    1 2 1 0 B    0 0  0  0
                 
+
contribution from horizontal linear interpolation:
                 B BD DB D
    0 1 2 1 C    0 0  0  0
    0 3 6 3 CD   1 3  3  1
    0 3 6 3 DC   2 6  6  2
    0 1 2 1 D    1 3  3  1

-
contribution from bilinear interpolation:
A              B
    1 2 1 0      0 0 0 0
    2 4 2 0      1 2 1 0
    1 2 1 0      2 4 2 0
    0 0 0 0      1 2 1 0

C              D
    0 1 2 1      0 0 0 0
    0 2 4 2      0 1 2 1
    0 1 2 1      0 2 4 2
    0 0 0 0      0 1 2 1

Each of the non-corner points thus contributes
according to one of the above rows and columns;
the contributions of the corner points add up to:

A              B
    1 0 0 0      0 0 0 0
    0 -4 -2 0    0 -2 -1 0
    0 -2 -1 0    0 -4 -2 0
    0 0 0 0      1 0 0 0

C              D
    0 0 0 1      0 0 0 0
    0 -2 -4 0    0 -1 -2 0
    0 -1 -2 0    0 -2 -4 0
    0 0 0 0      0 0 0 1

Since in the tensor-product patch the control points
are each multiplied by one of the products represented
in the squares (except for the factors of 3 missing from
the polynomials), this allows us to derive how the internal
control points need to be chosen in order to make the
Coons patch a special case of the tensor-product patch.
(The exterior control points coincide with the Coons
control points.) For instance, by picking out the numbers
in the upper left corner of the inner squares, we find
that the upper left internal control point is given by
1/9 of the following linear combination of the external
control points:

-4 6 0 -2
 6      3
 0      0
-2 3 0 -1

where the entries correspond to the external control points. 
*/
