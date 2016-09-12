/*
 * Copyright 2006 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.graphics;

public class Intersection implements Shape {
  Shape a;
  Shape b;
  
  public Intersection (Shape a,Shape b) {
    this.a = a;
    this.b = b;
  }

  public Intersection transformedBy (Transformation transform) {
    return new Intersection (a.transformedBy (transform),b.transformedBy (transform));
  }
  
  public Intersection intersectionWith (Shape shape) {
    return new Intersection (this,shape);
  }
  
  public boolean contains (Point point) {
    return a.contains (point) && b.contains (point);
  }

  public Rectangle getBoundingBox () {
    Rectangle boundingBox = new Rectangle (a.getBoundingBox ());
    boundingBox.intersectWith (b.getBoundingBox ());
    return boundingBox;
  }

  public Rectangle getBoundingBox (Transformation transform) {
    Rectangle boundingBox = new Rectangle (a.getBoundingBox (transform));
    boundingBox.intersectWith (b.getBoundingBox (transform));
    return boundingBox;
  }
}
