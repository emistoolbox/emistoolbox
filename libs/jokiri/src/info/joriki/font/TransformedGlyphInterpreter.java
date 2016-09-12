/*
 * Copyright 2006 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.font;

import info.joriki.graphics.Point;
import info.joriki.graphics.Transformation;
import info.joriki.util.NotImplementedException;
import info.joriki.util.NotTestedException;

public class TransformedGlyphInterpreter implements GlyphInterpreter {
  GlyphInterpreter interpreter;
  Transformation transform;
  Transformation relativeTransform;
  
  public TransformedGlyphInterpreter (GlyphInterpreter interpreter,Transformation transform) {
    this.interpreter = interpreter;
    this.transform = transform;
    relativeTransform = transform.linearPart ();
  }

  public void moveto (double x,double y) {
    Point p = transformedPoint (x,y);
    interpreter.moveto (p.x,p.y);
    throw new NotTestedException ();
  }

  public void lineto (double x,double y) {
    Point p = transformedPoint (x,y);
    interpreter.moveto (p.x,p.y);
    throw new NotTestedException ();
  }

  public void rmoveto (double dx,double dy) {
    Point p = relativeTransformedPoint (dx,dy);
    interpreter.rmoveto (p.x,p.y);
  }

  public void rlineto (double dx,double dy) {
    Point p = relativeTransformedPoint (dx,dy);
    interpreter.rlineto (p.x,p.y);
  }

  public void rrcurveto (double dx1,double dy1,double dx2,double dy2) {
    Point p1 = relativeTransformedPoint (dx1,dy1);
    Point p2 = relativeTransformedPoint (dx2,dy2);
    interpreter.rrcurveto (p1.x,p1.y,p2.x,p2.y);
    throw new NotTestedException ();
  }

  public void rrcurveto (double dx1,double dy1,double dx2,double dy2,double dx3,double dy3) {
    Point p1 = relativeTransformedPoint (dx1,dy1);
    Point p2 = relativeTransformedPoint (dx2,dy2);
    Point p3 = relativeTransformedPoint (dx3,dy3);
    interpreter.rrcurveto (p1.x,p1.y,p2.x,p2.y,p3.x,p3.y);
  }

  public void flex (double[] args) {
    throw new NotImplementedException ("transformed flex");
  }

  public void setAdvance (double x,double y) {
    Point p = relativeTransformedPoint (x,y);
    interpreter.setAdvance (p.x,p.y);
  }

  public void newpath () {
    interpreter.newpath ();
  }

  public void closepath () {
    interpreter.closepath ();
  }

  public void finish () {
    interpreter.finish ();
  }

  private Point transformedPoint (double x,double y) {
    Point p = new Point (x,y);
    p.transformBy (transform);
    return p;
  }

  private Point relativeTransformedPoint (double dx,double dy) {
    Point p = new Point (dx,dy);
    p.transformBy (relativeTransform);
    return p;
  }
}
