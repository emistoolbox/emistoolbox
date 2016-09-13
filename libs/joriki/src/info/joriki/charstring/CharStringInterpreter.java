/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.charstring;

public interface CharStringInterpreter
{
  void moveto (double x,double y);
  void rmoveto (double dx,double dy);
  void rlineto (double dx,double dy);
  void rrcurveto (double dx1,double dy1,
                  double dx2,double dy2,
                  double dx3,double dy3);
  void flex (double [] args);
  void setAdvance (double x,double y);
  void newpath ();
  void closepath (); // don't move!
  void finish ();
}
