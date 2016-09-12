/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.io.IOException;


public class PDFIndirectObject extends PDFAtom
{
  private static final byte [] referenceSuffix = " 0 R".getBytes ();

  PDFObject object;

  protected PDFIndirectObject () {}

  public PDFIndirectObject (PDFObject object)
  {
    this.object = object;
  }

  protected PDFObject getObject ()
  {
    return object;
  }

  protected void writeAtom (PDFObjectWriter objectWriter) throws IOException
  {
    PDFWriter writer = (PDFWriter) objectWriter;
    writer.print (writer.numberFor (getObject ()));
    writer.write (referenceSuffix);
  }

  public String toString ()
  {
    return getObject ().toString ();
  }

  public PDFObject resolve ()
  {
    return getObject ().resolve ();
  }

  public PDFObject resolveIndirection ()
  {
    return getObject ().resolveIndirection ();
  }

  public boolean equals (Object o)
  {
    throw new Error ("indirect objects being compared using equals");
    //    return o instanceof PDFIndirectObject &&
    //      ((PDFIndirectObject) o).object == object;
  }

  protected String displayString ()
  {
    throw new InternalError ("indirect object shouldn't be displayed");
  }
}
