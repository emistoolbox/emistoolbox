/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.io.IOException;
import java.io.InputStream;

public class PDFFileObjectReader extends PDFIndirectObjectReader
{
  PDFFileObjectReader (InputStream in,PDFFile dad)
  {
    super (in,dad);
  }

  public Object readObject () throws IOException
  {
    Object object = super.readObject ();
    if (object instanceof PDFDictionary)
      return dad.potentialStream ((PDFDictionary) object);
    if (object instanceof PDFString)
      dad.crypt.decrypt ((PDFString) object);
    return object;
  }
}
