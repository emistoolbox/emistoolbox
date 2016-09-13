/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.truetype;

public interface OutlineInterpreter
{
  void moveto (double x,double y);
  void lineto (double x,double y);
  void rrcurveto (double dx1,double dy1,double dx2,double dy2);

  void newpath ();
  // don't move! (actually, irrelevant for TrueType, but
  // this is combined with CharStringInterpreter.closepath
  // in GlyphInterpreter and should therefore be compatible.
  void closepath ();
  void finish ();
}
