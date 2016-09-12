/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

abstract public class PDFValue extends PDFAtom
{
  protected String getValue ()
  {
    return toString ();
  }

  abstract protected void setValue (String value);
}
