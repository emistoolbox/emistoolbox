/*
 * Copyright 2003 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf.filter;

import info.joriki.io.Readable;

import info.joriki.io.filter.ByteFilters;
import info.joriki.io.filter.BitStreamCoders;

import info.joriki.pdf.PDFDictionary;

public class LZWDecoder extends PredictingFilter
{
  public Readable getPredictable (Readable raw,PDFDictionary parameters)
  {
    int earlyChange = parameters == null ? 1 :
      parameters.getInt ("EarlyChange",1);

    return ByteFilters.getReadable
      (BitStreamCoders.getByteInputFilter
       (new info.joriki.compression.lzw.LZWDecoder (8,earlyChange),false),raw);
  }
}
