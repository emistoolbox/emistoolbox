/*
 * Copyright 2006 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.io.IOException;

abstract public class PDFAtom extends PDFObject {
  protected boolean write (PDFObjectWriter writer) throws IOException
  {
    writer.delimit ();
    writeAtom (writer);
    return false;
  }

  protected void writeAtom (PDFObjectWriter writer) throws IOException {
    writer.write (toString ());
  }

  protected String displayString () {
    return toString ();
  }
}
