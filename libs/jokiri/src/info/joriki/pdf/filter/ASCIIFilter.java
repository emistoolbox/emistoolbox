/*
 * Copyright 2005 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf.filter;

import info.joriki.compression.ascii.BaseDecoder;
import info.joriki.io.Readable;
import info.joriki.io.filter.ByteFilters;
import info.joriki.pdf.PDFDictionary;
import info.joriki.pdf.StreamFilter;
import info.joriki.util.Assertions;

abstract public class ASCIIFilter implements StreamFilter {
  BaseDecoder decoder;
  
  protected ASCIIFilter (BaseDecoder decoder) {
    this.decoder = decoder;
  }
  
  public Readable getReadable (Readable raw,PDFDictionary parameters)
  {
    decoder.reset ();
    Assertions.expect (parameters,null);
    return ByteFilters.getReadable (decoder,raw);
  }
}
