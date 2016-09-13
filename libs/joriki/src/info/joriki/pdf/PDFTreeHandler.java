/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

public interface PDFTreeHandler
{
  void handle (PDFObject key,PDFIndirectObject handler);
}
