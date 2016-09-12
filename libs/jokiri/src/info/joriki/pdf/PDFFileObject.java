/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.io.IOException;

public class PDFFileObject extends PDFIndirectObject
{
  PDFFile file;
  PDFObjectIdentifier identifier;

  public PDFFileObject (PDFFile file,int object,int generation)
  {
    this (file,new PDFObjectIdentifier (object,generation));
  }

  public PDFFileObject (PDFFile file,PDFObjectIdentifier identifier)
  {
    this.file = file;
    this.identifier = identifier;
  }

  protected PDFObject getObject ()
  {
    if (object == null)
      try {
        object = file.getObject (identifier);
      } catch (IOException ioe) {
        ioe.printStackTrace ();
        throw new RuntimeException ("could not retrieve object " + identifier);
      }
    return object;
  }

  public boolean equals (Object o)
  {
    if (!(o instanceof PDFFileObject))
      return false;
    PDFFileObject r = (PDFFileObject) o;
    return r.file == file && r.identifier.equals (identifier);
  }

  public String toString ()
  {
    return identifier.toString () + " R";
  }
}
