/*
 * Copyright 2007 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import info.joriki.awt.image.RGBColorModel;
import info.joriki.graphics.Point;
import info.joriki.graphics.Rectangle;
import info.joriki.graphics.Transformation;
import info.joriki.util.NotImplementedException;
import info.joriki.util.NotTestedException;

import java.awt.image.ImageProducer;
import java.awt.image.MemoryImageSource;

public class FunctionShading extends DomainShading {
  Transformation transform;
  
  FunctionShading (PDFDictionary dictionary,ResourceResolver resourceResolver) {
    super (dictionary,resourceResolver,2);
    transform = dictionary.getTransformation ("Matrix",Transformation.identity);
    if (!transform.equals (Transformation.identity))
      throw new NotImplementedException ("transform for function shading");
    if (boundingBox != null)
      throw new NotTestedException ("bounding box for function shading");
    if (backgroundColor != null)
      throw new NotTestedException ("background for function shading");
  }

  protected Rectangle getBoundingBox (Transformation transform) {
    if (!this.transform.equals (Transformation.identity))
      throw new NotImplementedException ("transformed bounding box for function shading");
    Rectangle boundingBox = new Rectangle (domain [0],domain [2],domain [1],domain [3]);
    return boundingBox.transformBy (transform);
  }
  
  protected void draw (int [] pixels,Transformation transform,int nsub,java.awt.Rectangle bbox,int background,boolean translucent) {
    if (!translucent)
      throw new NotTestedException ("opaque function shading");
    if (!this.transform.equals (Transformation.identity))
      throw new NotImplementedException ("transformed bounding box for function shading");
    Transformation inverse = transform.inverse ();
    Point p = new Point ();
    Point t = new Point ();
    float [] z = new float [2];
    for (int y = 0,k = 0;y < bbox.height;y++)
      for (int x = 0;x < bbox.width;x++,k++) {
        p.x = bbox.x + x;
        p.y = bbox.y + y;
        t.productOf (p,inverse);
        int pixel;
        if (domain [0] <= t.x && t.x <= domain [1] &&
            domain [2] <= t.y && t.y <= domain [3]) {
          z [0] = (float) t.x;
          z [1] = (float) t.y;
          float [] color;
          if (function != null)
            color = function.f (z);
          else if (functions != null) {
            color = new float [functions.length];
            for (int i = 0;i < color.length;i++)
              color [i] = functions [i].f (z) [0];
          }
          else
            throw new InternalError ();
          pixel = baseColorSpace.mappedBase.toRGB (color);
          if (translucent)
            pixel |= 0xff000000;
        }
        else
          pixel = background;

        pixels [k] = pixel;
      }
  }
}
