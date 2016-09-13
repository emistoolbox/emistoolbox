/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

public abstract class PDFNumber extends PDFValue
{
  abstract public double doubleValue ();

  public float floatValue ()
  {
    return (float) doubleValue ();
  }

  public boolean equals (Object o)
  {
    return o instanceof PDFNumber &&
      ((PDFNumber) o).doubleValue () == doubleValue ();
  }
}
