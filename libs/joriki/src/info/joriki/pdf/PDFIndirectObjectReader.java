/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;

import java.util.Stack;

public class PDFIndirectObjectReader extends PDFObjectReader
{
  PDFFile dad;
  Stack undone = new Stack ();

  PDFIndirectObjectReader (InputStream in,PDFFile dad)
  {
    super (in,true);
    this.dad = dad;
  }

  PDFObject readExternalObject () throws IOException
  {
    tok.flush ();
    undone.removeAllElements ();
    return (PDFObject) readObject ();
  }

  Object readCachedObject () throws IOException
  {
    return undone.isEmpty () ? super.readObject () : undone.pop ();
  }

  public Object readObject () throws IOException
  {
    Object obj = readCachedObject ();
    if (obj instanceof PDFInteger) {
      Object gen = readCachedObject ();
      if (gen instanceof PDFInteger) {
        Object r = readCachedObject ();
        if (r instanceof String) {
          if (r.equals ("R"))
            return new PDFFileObject
            (dad,
                ((PDFInteger) obj).val,
                ((PDFInteger) gen).val);
          throw new StreamCorruptedException ("unknown keyword " + r);
        }
        undone.push (r);
      }
      undone.push (gen);
    }
    return obj;
  }
}
