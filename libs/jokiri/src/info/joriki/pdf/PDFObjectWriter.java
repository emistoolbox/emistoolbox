/*
 * Copyright 2007 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.io.IOException;
import java.io.OutputStream;

import info.joriki.io.CountingOutputStream;

public class PDFObjectWriter extends CountingOutputStream {
  Crypt crypt = new Crypt (null);
  
  boolean isDelimited;

  public PDFObjectWriter (OutputStream out) {
    super (out);
  }

  void delimit () throws IOException {
    if (!isDelimited)
      write (' ');
  }
  
  void write (String string) throws IOException {
    write (string.getBytes ());
  }
  
  void write (PDFObject object) throws IOException {
    isDelimited = object.write (this);
  }
  
  void setOutputStream (OutputStream out) {
    this.out = out;
  }
}
