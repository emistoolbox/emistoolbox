/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

class PDFObjectIdentifier
{
  int object;
  int generation;

  PDFObjectIdentifier (int object)
  {
    this (object,0);
  }

  PDFObjectIdentifier (int object,int generation)
  {
    this.object = object;
    this.generation = generation;
  }

  public boolean equals (Object o)
  {
    if (!(o instanceof PDFObjectIdentifier))
      return false;
    PDFObjectIdentifier r = (PDFObjectIdentifier) o;
    return r.object == object && r.generation == generation;
  }

  public int hashCode ()
  {
    return object + (generation << 16);
  }

  public String toString ()
  {
    return object + " " + generation;
  }
}
