/*
 * Copyright 2006 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.graphics;

public class TransformedShape implements Shape {
  Shape shape;
  Transformation transform;

  public TransformedShape (Shape shape,Transformation transform) {
    this.shape = shape;
    this.transform = transform;
  }

  public TransformedShape transformedBy (Transformation transform) {
    return new TransformedShape (shape,new Transformation (this.transform,transform));
  }

  public Shape intersectionWith (Shape shape) {
    return new Intersection (this,shape);
  }

  public boolean contains (Point point) {
    return shape.contains (point.transformedBy (transform.inverse ()));
  }

  public Rectangle getBoundingBox () {
    return shape.getBoundingBox (transform);
  }

  public Rectangle getBoundingBox (Transformation transform) {
    return shape.getBoundingBox (new Transformation (this.transform,transform));
  }
}
