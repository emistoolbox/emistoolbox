/*
 * Copyright 2003 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf.filter;

import info.joriki.io.Readable;

import info.joriki.pdf.StreamFilter;
import info.joriki.pdf.PDFDictionary;

class IdentityFilter implements StreamFilter
{
  public Readable getReadable (Readable raw,PDFDictionary parameters)
  {
    return raw;
  }
}

