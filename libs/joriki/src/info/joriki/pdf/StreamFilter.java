/*
 * Copyright 2003 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import info.joriki.io.Readable;

public interface StreamFilter
{
  Readable getReadable (Readable raw,PDFDictionary parameters);
}

