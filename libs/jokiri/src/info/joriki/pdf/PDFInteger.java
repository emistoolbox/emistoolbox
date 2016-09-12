/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import info.joriki.util.General;
import info.joriki.util.Assertions;

public class PDFInteger extends PDFNumber implements Comparable
{
  int val;

  public PDFInteger ()
  {
    this (0);
  }
  
  public PDFInteger (int val)
  {
    this.val = val;
  }

  public String toString ()
  {
    return Integer.toString (val);
  }

  public int intValue ()
  {
    return val;
  }

  public double doubleValue ()
  {
    return val;
  }

  public int rotationValue ()
  {
    Assertions.expect (val % 90,0);
    return (val / 90) & 3;
  }
  
  public int compareTo (Object o)
  {
    return val - ((PDFInteger) o).val;
  }

  protected void setValue (String value)
  {
    try {
      val = General.intValue (value);
    } catch (NumberFormatException nfe) {}
  }
  
  public Integer toObject () {
    return val; 
  }
}
