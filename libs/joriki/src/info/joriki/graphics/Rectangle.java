/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.graphics;

import info.joriki.util.Assertions;

public class Rectangle implements Shape
{
  // for a "normal" rectangle, xmin <= xmax and ymin <= ymax
  // for an "empty" rectangle, all coordinates are infinite.
  // xmin > xmax or ymin > ymax is not enough, since in that
  // case adding a point would create a non-point rectangle.
  public double xmin,ymin;
  public double xmax,ymax;

  public Rectangle ()
  {
    empty ();
  }
  
  public Rectangle (Point p)
  {
    xmin = xmax = p.x;
    ymin = ymax = p.y;
  }

  public Rectangle (Rectangle r)
  {
    this (r.xmin,r.ymin,r.xmax,r.ymax);
  }

  public Rectangle (double xmin,double ymin,double xmax,double ymax)
  {
    this.xmin = xmin;
    this.ymin = ymin;
    this.xmax = xmax;
    this.ymax = ymax;
  }

  public Rectangle (double [] bbox)
  {
    this (bbox [0],bbox [1],bbox [2],bbox [3]);
  }

  public Rectangle (Point p,double w,double h)
  {
    this (p,new Point (p.x + w,p.y + h));
  }

  public Rectangle (Point p1,Point p2)
  {
    init (p1,p2);
  }

  private void check ()
  {
    Assertions.expect (xmax >= xmin);
    Assertions.expect (ymax >= ymin);
  }

  public void shiftBy (double dx,double dy)
  {
    xmin -= dx;
    ymin -= dy;
    xmax -= dx;
    ymax -= dy;
  }

  public void growBy (double width)
  {
    xmin -= width;
    ymin -= width;
    xmax += width;
    ymax += width;
  }
  
  public void shrinkBy (double width)
  {
    xmin += width;
    ymin += width;
    xmax -= width;
    ymax -= width;
    if (xmax < xmin)
      xmax = xmin = (xmin + xmax) / 2;
    if (ymax < ymin)
      ymax = ymin = (ymin + ymax) / 2;
  }

  private void init (Point p1,Point p2)
  {
    xmin = Math.min (p1.x,p2.x);
    xmax = Math.max (p1.x,p2.x);
    ymin = Math.min (p1.y,p2.y);
    ymax = Math.max (p1.y,p2.y);
  }

  public void empty ()
  {
    xmin = ymin = Double.POSITIVE_INFINITY;
    xmax = ymax = Double.NEGATIVE_INFINITY;
  }

  public final double width ()
  {
    return xmax - xmin;
  }

  public final double height ()
  {
    return ymax - ymin;
  }

  public void add (Point p)
  {
    add (p.x,p.y);
  }

  public void add (double [] p)
  {
    add (p [0],p [1]);
  }

  public void add (double x,double y)
  {
    xmin = Math.min (xmin,x);
    ymin = Math.min (ymin,y);
    xmax = Math.max (xmax,x);
    ymax = Math.max (ymax,y);
  }

  public void add (Rectangle r)
  {
    xmin = Math.min (xmin,r.xmin);
    ymin = Math.min (ymin,r.ymin);
    xmax = Math.max (xmax,r.xmax);
    ymax = Math.max (ymax,r.ymax);
  }
  
  public void add (Shape shape) {
    add (shape.getBoundingBox ());
  }

  public Point [] corners ()
  {
    Point [] res = new Point [4];
    res [0] = new Point (xmin,ymin);
    res [1] = new Point (xmax,ymin);
    res [2] = new Point (xmax,ymax);
    res [3] = new Point (xmin,ymax);
    return res;
  }

  public boolean contains (Rectangle r)
  {
    return
      xmin <= r.xmin && r.xmax <= xmax &&
      ymin <= r.ymin && r.ymax <= ymax;
  }

  public boolean intersects (Rectangle r) {
    return
      r.xmin <= xmax && xmin <= r.xmax &&
      r.ymin <= ymax && ymin <= r.ymax;
  }
  
  public void intersectWith (Rectangle r)
  {
    if (intersects (r)) {
      xmin = Math.max (xmin,r.xmin);
      xmax = Math.min (xmax,r.xmax);
      ymin = Math.max (ymin,r.ymin);
      ymax = Math.min (ymax,r.ymax);
    }
    else
      empty ();
  }

  public double [] toDoubleArray ()
  {
    return new double [] {xmin,ymin,xmax,ymax};
  }

  public Rectangle transformBy (Transformation t)
  {
    if (isFinite ())
    {
      Point [] corners = corners ();
      empty ();
      for (int i = 0;i < corners.length;i++)
      {
        corners [i].transformBy (t);
        add (corners [i]);
      }
    }
    return this;
  }
  
  public Shape transformedBy (Transformation transform) {
    return transform.isAxisParallel () ? new Rectangle (this).transformBy (transform) : new TransformedShape (this,transform);
  }

  public boolean isFinite () // i.e. neither infinite nor empty
  {
    return 1 / xmin != 0;
  }
  
  public boolean isEmpty ()
  {
    return xmax < xmin || ymax < ymin;
  }

  public Point getCenter ()
  {
    return new Point ((xmin + xmax) / 2,(ymin + ymax) / 2);
  }

  public static Rectangle getInfinitePlane ()
  {
    return new Rectangle
    (Double.NEGATIVE_INFINITY,
     Double.NEGATIVE_INFINITY,
     Double.POSITIVE_INFINITY,
     Double.POSITIVE_INFINITY);
  }

  public void round ()
  {
    xmin = Math.floor (xmin);
    ymin = Math.floor (ymin);
    xmax = Math.ceil  (xmax);
    ymax = Math.ceil  (ymax);
  }

  public java.awt.Rectangle toAWTRectangle ()
  {
    return new java.awt.Rectangle
      ((int) xmin,(int) ymin,(int) width (),(int) height ());
  }

  public boolean contains (Point p)
  {
    return
      xmin <= p.x && p.x <= xmax &&
      ymin <= p.y && p.y <= ymax;
  }

  public String toString ()
  {
    return "[" + xmin + "," + ymin + "," + xmax + "," + ymax + "]";
  }

  public boolean equals (Object o)
  {
    if (!(o instanceof Rectangle))
      return false;
    Rectangle r = (Rectangle) o;
    return
      xmin == r.xmin &&
      ymin == r.ymin &&
      xmax == r.xmax &&
      ymax == r.ymax;
  }

  public Shape intersectionWith (Shape shape) {
    if (shape instanceof Rectangle) {
      Rectangle rectangle = new Rectangle ((Rectangle) shape);
      rectangle.intersectWith (this);
      return rectangle;
    }
    return new Intersection (this,shape);
  }

  public Rectangle getBoundingBox () {
    return this;
  }

  public Rectangle getBoundingBox (Transformation transform) {
    return new Rectangle (this).transformBy (transform);
  }
}
