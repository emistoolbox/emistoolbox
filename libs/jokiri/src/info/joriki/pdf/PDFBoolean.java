/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

public class PDFBoolean extends PDFValue
{
  boolean val;

  public PDFBoolean ()
  {
    this (true);
  }
  
  public PDFBoolean (boolean val)
  {
    this.val = val;
  }

  public String toString ()
  {
    return val ? "true" : "false";
  }

  protected void setValue (String value)
  {
    if (value.equals ("true"))
      val = true;
    else if (value.equals ("false"))
      val = false;
  }
  
  public boolean booleanValue () {
    return val;
  }

  public boolean equals (Object o)
  {
    return o instanceof PDFBoolean &&
      ((PDFBoolean) o).val == val;
  }

  public Boolean toObject () {
    return val;
  }
}
