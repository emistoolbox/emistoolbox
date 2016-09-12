/*
 * Copyright 2003 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf.filter;

import info.joriki.io.Readable;

import info.joriki.io.filter.ByteFilters;

import info.joriki.pdf.StreamFilter;
import info.joriki.pdf.PDFDictionary;

import info.joriki.util.Assertions;

public class RunLengthDecoder implements StreamFilter
{
  public Readable getReadable (Readable raw,PDFDictionary parameters)
  {
    Assertions.expect (parameters,null);
    return ByteFilters.getReadable
      (new info.joriki.compression.runlength.RunLengthDecoder (),raw);
  }
}
