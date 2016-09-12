/*
 * Copyright 2003 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.charstring;

public class CharStringAdapter implements CharStringInterpreter
{
  public void moveto (double x,double y) {}
  public void rmoveto (double dx,double dy) {}
  public void rlineto (double dx,double dy) {}
  public void rrcurveto (double dx1,double dy1,
                         double dx2,double dy2,
                         double dx3,double dy3) {}
  public void flex (double [] args) {}
  public void setAdvance (double x,double y) {}
  public void newpath () {}
  public void closepath () {}
  public void finish () {}
}
