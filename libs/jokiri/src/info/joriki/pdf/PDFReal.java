/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import info.joriki.util.General;

public class PDFReal extends PDFNumber
{
  double val;

  public PDFReal ()
  {
    this (0);
  }
    
  public PDFReal (double val)
  {
    this.val = val;
  }

  public String toString ()
  {
    return val == (int) val ?
      Integer.toString ((int) val) :
      General.preciseString (val);
  }

  public double doubleValue ()
  {
    return val;
  }

  protected void setValue (String value)
  {
    try {
      val = Double.parseDouble (value);
    } catch (NumberFormatException nfe) {}
  }
  
  public Double toObject () {
    return val;
  }
}
