/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.graphics;

public class Point
{
  public double x,y;

  public Point () {}

  public Point (double x,double y)
  {
    this.x = x;
    this.y = y;
  }
  
  public Point (Point p)
  {
    this.x = p.x;
    this.y = p.y;
  }
  
  public Point (Point p,Transformation t)
  {
    productOf (p,t);
  }
  
  final public Point transformBy (Transformation t)
  {
    productOf (this,t);
    return this;
  }
  
  final public Point transformedBy (Transformation t) {
    return new Point (this).transformBy (t);
  }
  
  final public void productOf (Point p,Transformation t)
  {
    double [] m = t.matrix;
    double newX = m [0] * p.x + m [2] * p.y + m [4];
    double newY = m [1] * p.x + m [3] * p.y + m [5];
    x = newX;
    y = newY;
  }
  
  final public void subtract (Point p)
  {
    x -= p.x;
    y -= p.y;
  }

  final public double distanceFrom (Point p)
  {
    double dx = x - p.x;
    double dy = y - p.y;
    return Math.sqrt (dx * dx + dy * dy);
  }
  
  final public void round ()
  {
    x = Math.round (x);
    y = Math.round (y);
  }
  
  public String toString ()
  {
    return x + " " + y;
  }
  
  public boolean equals (Object o) {
    if (!(o instanceof Point))
      return false;
    Point p = (Point) o;
    return p.x == x && p.y == y;
  }
}
