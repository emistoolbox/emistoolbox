/*
 * Copyright 2003 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf.filter;

import info.joriki.io.Readable;
import info.joriki.pdf.PDFDictionary;
import info.joriki.pdf.StreamFilter;

// JPEG images are handled by pdf.ImageHandler
public class DCTDecoder implements StreamFilter {
  public Readable getReadable (Readable raw,PDFDictionary parameters) {
    if (parameters != null) {
      parameters.ignore ("Rows");
      parameters.ignore ("Columns");
      parameters.ignore ("Colors");
      parameters.ignore ("QFactor");
      parameters.ignore ("Blend");
      parameters.ignore ("HSamples");
      parameters.ignore ("VSamples");
    }
    return raw;
  }
}
