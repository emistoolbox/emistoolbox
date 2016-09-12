/*
 * Copyright 2006 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.graphics;

public interface Shape {
  Rectangle getBoundingBox (Transformation transform);
  Rectangle getBoundingBox ();
  Shape transformedBy (Transformation transform);
  Shape intersectionWith (Shape shape);
  boolean contains (Point point);
}
