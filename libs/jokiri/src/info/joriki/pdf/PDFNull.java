/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

public class PDFNull extends PDFAtom
{
  public static final PDFNull nullObject = new PDFNull ();

  private PDFNull () {}

  public String toString ()
  {
    return "null";
  }

  public PDFObject resolve ()
  {
    return null;
  }
}
