/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.font;

import java.util.List;
import java.util.ArrayList;

public class GlyphCanonicalizer implements GlyphInterpreter {

  List list = new ArrayList ();
  
  public GlyphCanonicalizer (GlyphProvider provider)
  {
    provider.interpret(this);
  }
  
  public boolean equals (Object o)
  {
    return o instanceof GlyphCanonicalizer && list.equals (((GlyphCanonicalizer) o).list);
  }
  
  private void command (char command)
  {
    list.add (new Character (command));
  }
  
  private void parameter (double parameter)
  {
    list.add (new Double (parameter));
  }
  
  public void moveto(double x, double y) {
    command ('M');
    parameter (x);
    parameter (y);
  }

  public void rmoveto(double dx, double dy) {
    command ('m');
    parameter (dx);
    parameter (dy);
  }

  public void lineto(double x, double y) {
    command ('L');
    parameter (x);
    parameter (y);
  }

  public void rlineto(double dx, double dy) {
    command ('l');
    parameter (dx);
    parameter (dy);
  }

  public void rrcurveto(double dx1, double dy1, double dx2, double dy2) {
    command ('r');
    parameter (dx1);
    parameter (dy1);
    parameter (dx2);
    parameter (dy2);
  }

  public void rrcurveto(
    double dx1,double dy1,
    double dx2,double dy2,
    double dx3,double dy3)
  {
    command ('r');
    parameter (dx1);
    parameter (dy1);
    parameter (dx2);
    parameter (dy2);
    parameter (dx3);
    parameter (dy3);
  }

  public void flex(double[] args) {
    command ('f');
    for (int i = 0;i < args.length;i++)
      parameter (args [i]);
  }

  public void setAdvance(double x, double y) {
    command ('s');
    parameter (x);
    parameter (y);
  }

  public void newpath() {
    command ('n');
  }

  public void closepath() {
    command ('c');
  }

  public void finish() {
    command ('f');
  }
}
