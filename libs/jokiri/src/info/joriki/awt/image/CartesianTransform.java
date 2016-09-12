/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image;

import info.joriki.util.Assertions;
import info.joriki.util.CloneableObject;

import info.joriki.graphics.Transformation;

/**
 * An instance of this class represents one of the eight possible
 * rotations (including 3D ones that involve reflection if viewed
 * in the plane) that transform an axis-parallel rectangle in the
 * x-y plane into an axis-parallel rectangle in the x-y plane.
 */
public class CartesianTransform extends CloneableObject
{
  // these flags are of course non-commutative; they are to be
  // interpreted in ascending order, i.e. SWAP | INVERTY means
  // first swap x and y, then invert y
  public final static byte SWAP    = 1;
  public final static byte INVERTX = 2;
  public final static byte INVERTY = 4;

  // these are names for all the combinations
  public final static byte IDENTITY   = 0;                    //       ab
  public final static byte DIAGONAL   = SWAP;                 //       cd    ac
  public final static byte INVERT_X   = INVERTX;              // ba          bd
  public final static byte ROTATE270  = SWAP | INVERTX;       // dc    ca
  public final static byte INVERT_Y   = INVERTY;              //       db    cd
  public final static byte ROTATE90   = SWAP | INVERTY;       // bd          ab
  public final static byte ROTATE180  = INVERTX | INVERTY;    // ac    dc
  public final static byte CODIAGONAL = SWAP | INVERTX | INVERTY; //   ba    db
  //                                                                         ca

  boolean [] flags = new boolean [3]; // {swaps,invertsX,invertsY};

  public CartesianTransform ()
  {
    this (IDENTITY);
  }

  public CartesianTransform (byte transform)
  {
    Assertions.limit (transform,0,7);
    for (int i = 0;i < 3;i++)
      flags [i] = (transform & (1 << i)) != 0;
  }

  public CartesianTransform (CartesianTransform t1,CartesianTransform t2)
  {
    flags [0] = t1.flags [0] ^ t2.flags [0];
    for (int i = 1;i <= 2;i++)
      flags [i] = t1.flags [t2.flags [0] ? 3 - i : i] ^ t2.flags [i];
  }

  // construct a cartesian transform "near" to t -- the only guarantee
  // in "nearness" is that if t is a similarity transform, the cartesian
  // transform is as close as possible to its rotation/reflection
  // component in the obvious sense
  public CartesianTransform (Transformation t)
  {
    double [] matrix = t.matrix;
    flags [0] = Math.abs (matrix [0] * matrix [3]) < Math.abs (matrix [1] * matrix [2]);
    flags [1] = matrix [flags [0] ? 2 : 0] < 0;
    flags [2] = matrix [flags [0] ? 1 : 3] < 0;
  }

  public Transformation toTransformation ()
  {
    setAffineParameters ();
    return new Transformation (new double [] {a00,a01,a10,a11,0,0});
  }

  public boolean isTrivial ()
  {
    for (int i = 0;i < 3;i++)
      if (flags [i])
        return false;
    return true;
  }

  public boolean swaps ()
  {
    return flags [0];
  }

  public boolean invertsX ()
  {
    return flags [1];
  }

  public boolean invertsY ()
  {
    return flags [2];
  }

  private int a00,a01;
  private int a10,a11;

  private void setAffineParameters ()
  {
    int x = invertsX () ? -1 : 1;
    int y = invertsY () ? -1 : 1;

    if (swaps ())
      {
        a10 = x;
        a01 = y;
      }
    else
      {
        a00 = x;
        a11 = y;
      }
  }

  private int a,b,c;

  public void setIndexDimensions (int w,int h)
  {
    setAffineParameters ();
    int wp = swaps () ? h : w;
    a = a10 * wp + a00;
    b = a11 * wp + a01;
    c = (h * w - (a * (w - 1) + b * (h - 1) + 1)) / 2;
  }

  final public int index (int x,int y)
  {
    return a * x + b * y + c;
  }

  public void debug ()
  {
    System.out.println (a00 + " " + a01);
    System.out.println (a10 + " " + a11);
    System.out.println (a + " " + b + " " + c);
  }

  public boolean equals (Object o)
  {
    if (!(o instanceof CartesianTransform))
      return false;
    for (int i = 0;i < 3;i++)
      if (flags [i] != ((CartesianTransform) o).flags [i])
        return false;
    return true;
  }

  public int hashCode ()
  {
    int hashCode = 0;
    for (int i = 0;i < 3;i++)
      {
        hashCode <<= 1;
        if (flags [i])
          hashCode |= 1;
      }
    return hashCode;
  }
}
