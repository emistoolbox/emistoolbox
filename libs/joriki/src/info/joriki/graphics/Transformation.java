/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.graphics;

import info.joriki.util.General;
import info.joriki.util.Assertions;

public class Transformation
{
  final static int precision = 7;

  public final static Transformation identity = new Transformation ();

  public double [] matrix;

  public Transformation ()
  {
    matrix = new double [6];
    matrix [0] = matrix [3] = 1;
  }
  
  public Transformation (double ... matrix)
  {
    Assertions.expect (matrix.length,6);
    this.matrix = matrix;
  }

  public Transformation (Transformation t)
  {
    matrix = t.matrix.clone ();
  }

  public Transformation (Transformation t1,Transformation t2)
  {
    matrix = new double [6];
    productOf (t1,t2);
  }
  
  public void transform (double [] p)
  {
    double x = matrix [0] * p [0] + matrix [2] * p [1] + matrix [4];
    double y = matrix [1] * p [0] + matrix [3] * p [1] + matrix [5];
    p [0] = x;
    p [1] = y;
  }

  public void transform (float [] p)
  {
    double x = matrix [0] * p [0] + matrix [2] * p [1] + matrix [4];
    double y = matrix [1] * p [0] + matrix [3] * p [1] + matrix [5];
    p [0] = (float) x;
    p [1] = (float) y;
  }

  boolean equals (Object o,int n)
  {
    if (!(o instanceof Transformation))
      return false;
    double [] m = ((Transformation) o).matrix;
    for (int i = 0;i < n;i++)
      if (matrix [i] != m [i])
        return false;
    return true;
  }

  public boolean equals (Object o)
  {
    return equals (o,6);
  }

  public boolean equalsUpToTranslation (Object o)
  {
    return equals (o,4);
  }

  final public Transformation linearPart ()
  {
    Transformation linearPart = new Transformation (this);
    linearPart.matrix [4] = linearPart.matrix [5] = 0;
    return linearPart;
  }

  final public Transformation inverse ()
  {
    Transformation inverse = new Transformation ();
    inverse.inverseOf (this);
    return inverse;
  }

  final public void inverseOf (Transformation t)
  {
    double [] m = t.matrix;
    double d = 1 / t.determinant ();

    matrix [0] = d * m [3];
    matrix [3] = d * m [0];
    matrix [1] = -d * m [1];
    matrix [2] = -d * m [2];
      
    matrix [4] = d * (m [2] * m [5] - m [3] * m [4]);
    matrix [5] = d * (m [1] * m [4] - m [0] * m [5]);
  }

  final public void concat (Transformation t)
  {
    productOf (t,this);
  }

  /*
           a b 0     A B 0 
(u v 1)    c d 0     C D 0 
           x y 1     X Y 1
  */

  // t2 can be this, t1 can't.
  final public void productOf (Transformation t1,Transformation t2)
  {
    double [] m1 = t1.matrix;
    double [] m2 = t2.matrix;

    matrix [4] = m2 [0] * m1 [4] + m2 [2] * m1 [5] + m2 [4];
    matrix [5] = m2 [1] * m1 [4] + m2 [3] * m1 [5] + m2 [5];

    double x,y;

    x = m2 [0] * m1 [0] + m2 [2] * m1 [1];
    y = m2 [0] * m1 [2] + m2 [2] * m1 [3];
    matrix [0] = x;
    matrix [2] = y;

    x = m2 [1] * m1 [0] + m2 [3] * m1 [1];
    y = m2 [1] * m1 [2] + m2 [3] * m1 [3];
    matrix [1] = x;
    matrix [3] = y;
  }

  // returns the moduli of the eigenvalues of the transformation in increasing order
  public double [] semiaxes ()
  {
    double half = (matrix [0] - matrix [3]) / 2;
    double ave = (matrix [0] + matrix [3]) / 2;
    double arg = half * half + matrix [1] * matrix [2];
    if (arg >= 0)
      {
        double del = Math.sqrt (arg);
        double sum = Math.abs (ave + del);
        double dif = Math.abs (ave - del);
        return new double [] {Math.min (sum,dif),Math.max (sum,dif)};
      }
    else
      {
        double abs = Math.sqrt (arg + ave * ave);
        return new double [] {abs,abs};
      }
  }

  public double determinant ()
  {
    return matrix [0] * matrix [3] - matrix [1] * matrix [2];
  }

  // SVG format
  public String toString ()
  {
    StringBuilder stringBuilder = new StringBuilder ();
    boolean noangle = matrix [1] == 0 && matrix [2] == 0;
    boolean noscale = matrix [0] == 1 && matrix [3] == 1;
    boolean notrans = matrix [4] == 0 && matrix [5] == 0;

    if (noangle)
      {
        if (noscale)
          {
            if (notrans)
              return null;
            append ("translate",stringBuilder,4,5,0);
          }
        else if (notrans)
          append ("scale",stringBuilder,0,3,matrix [0]);
      }

    if (stringBuilder.length () == 0)
      {
        stringBuilder.append ("matrix(");
        General.append (stringBuilder,matrix,precision);
      }

    stringBuilder.append (')');

    return stringBuilder.toString ();
  }

  void append (String type,StringBuilder stringBuilder,int i1,int i2,double implied)
  {
    stringBuilder.append (type).append ('(');
    General.append (stringBuilder,matrix [i1],precision);
    if (matrix [i2] != implied)
      {
        stringBuilder.append (' ');
        General.append (stringBuilder,matrix [i2],precision);
      }
  }

  public final void copy (Transformation t)
  {
    copy (t.matrix);
  }
  
  public final void copy (double [] m)
  {
    Assertions.expect (m.length,6);
    System.arraycopy (m,0,matrix,0,6);
  }

  public Object clone ()
  {
    return new Transformation (matrix.clone ());
  }

  public void translateBy (double x,double y)
  {
    matrix [4] += x * matrix [0] + y * matrix [2];
    matrix [5] += x * matrix [1] + y * matrix [3];
  }

  public void scaleBy (double scale)
  {
    for (int i = 0;i < 4;i++)
      matrix [i] *= scale;
  }

  public static Transformation scalingBy (double scale)
  {
    return scalingBy (scale,scale);
  }
  
  public static Transformation scalingBy (double xscale,double yscale)
  {
    return new Transformation (new double [] {xscale,0,0,yscale,0,0});
  }
  
  public static Transformation rotationThrough (double phi)
  {
    double cos = Math.cos (phi);
    double sin = Math.sin (phi);
    return new Transformation (new double [] {cos,-sin,sin,cos,0,0});
  }

  public static Transformation translationBy (double x,double y)
  {
    return new Transformation (new double [] {1,0,0,1,x,y});
  }
  
  public static Transformation reflectionInX ()
  {
    return new Transformation (new double [] {-1,0,0,1,0,0});
  }
  
  public static Transformation reflectionInY ()
  {
    return new Transformation (new double [] {1,0,0,-1,0,0});
  }
  
  public static Transformation clockwiseRotation ()
  {
    return new Transformation (new double [] {0,1,-1,0,0,0});
  }

  public static Transformation counterclockwiseRotation ()
  {
    return new Transformation (new double [] {0,-1,1,0,0,0});
  }

  public double averageScale ()
  {
    return Math.sqrt (Math.abs (determinant ()));
  }

  /* This is on the model

             p     1      c s
     this =    r   q 1   -s c  (and possibly a reflection)

     which is useful for text: there are separate horizontal
     and vertical scales, an "obliqueness" and a rotation.
     horizontalScale () returns p,
     verticalScale () returns r,
     obliqueness () returns q, and
     rotationAngle () returns phi with c = cos (phi), s = sin (phi)

     turns out this is also good for rotated images
     (as treated by pdf.ImageRectifier in 2004COROLLA-ae.pdf)
     since there, too, one scales first and then rotates
  */

  public double horizontalScale ()
  {
    double a = matrix [0];
    double b = matrix [1];
    return Math.sqrt (a*a + b*b);
  }

  public double verticalScale ()
  {
    return Math.abs (determinant ()) / horizontalScale ();
  }

  public double obliqueness ()
  {
    return (matrix [0] * matrix [2] + matrix [1] * matrix [3]) / determinant ();
  }

  public double rotationAngle ()
  {
    return Math.atan2 (matrix [1],matrix [0]);
  }

  public boolean isTrivial ()
  {
    return equals (identity);
  }

  // factorize this into (this * return value),
  // where the new value of this involves only
  // scaling and translation
  public Transformation factorize (double scale)
  {
    double [] remainder = new double [6];
    for (int i = 0;i < 6;i++)
      remainder [i] = i <= 3 ? matrix [i] / scale : 0;
    Transformation result = new Transformation (remainder);
    double d = 1 / result.determinant ();
    double x = matrix [4];
    double y = matrix [5];
    matrix [0] = matrix [3] = scale;
    matrix [1] = matrix [2] = 0;
    matrix [4] = d * (remainder [3] * x - remainder [2] * y);
    matrix [5] = d * (remainder [0] * y - remainder [1] * x);
    return result;
  }

  // a * {{c,s},{-s,c}}, possibly times a reflection
  public boolean isSimilarity ()
  {
    return
      Math.abs (matrix [0]) == Math.abs (matrix [3]) &&
      Math.abs (matrix [1]) == Math.abs (matrix [2]) &&
      (matrix [0] * matrix [1] * matrix [2] * matrix [3] <= 0);
  }
  
  public boolean isAxisParallel ()
  {
    return matrix [1] == 0 && matrix [2] == 0;
  }
  
  public boolean isLinear ()
  {
    return matrix [4] == 0 && matrix [5] == 0;
  }
  
  public boolean isScaling () {
    return isAxisParallel () && isLinear ();
  }

  public static Transformation matchBoxes (Rectangle form,Rectangle to) {
    return matchBoxes (form.toDoubleArray(),to.toDoubleArray());
  }
  
  // both boxes in the form [x1 y1 x2 y2]
  public static Transformation matchBoxes (double [] from,double [] to)
  {
    double [] matrix = new double [6];

    for (int i = 0;i < 2;i++)
      {
        double den = 1 / (from [i + 2] - from [i]);
        matrix [3 * i] = (to [i + 2] - to [i]) * den;
        matrix [4 + i] = (from [i + 2] * to [i] - from [i] * to [i + 2]) * den;
      }

    return new Transformation (matrix);
  }

  public static Transformation matchPoints (Point from,Point to)
  {
    return translationBy (to.x - from.x,to.y - from.y);
  }
}
