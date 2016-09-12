/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import info.joriki.util.CloneableObject;
import info.joriki.util.NotImplementedException;

import java.io.IOException;

abstract public class PDFObject extends CloneableObject
{
  public PDFObject resolve () // resolves null and indirection
  {
    return this;
  }
  
  public PDFObject resolveIndirection () // resolves only indirection
  {
    return this;
  }

  public Object toObject () {
    throw new NotImplementedException ("object conversion for PDF object type " + getClass ().getName ());
  }

  // a shallow copy of this object
  public PDFObject clone () {
    return (PDFObject) super.clone ();
  }
  
  abstract protected boolean write (PDFObjectWriter writer) throws IOException;
  abstract protected String displayString ();
}
