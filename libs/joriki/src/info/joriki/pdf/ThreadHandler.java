/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

public interface ThreadHandler
{
  void handleThread (PDFDictionary thread);
  void handleBead (PDFBead bead);
  void finishThread ();
}
