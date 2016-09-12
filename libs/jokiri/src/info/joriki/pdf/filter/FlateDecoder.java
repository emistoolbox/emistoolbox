/*
 * Copyright 2003 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf.filter;

import java.io.InputStream;

import java.util.zip.ZipException;

import info.joriki.io.Util;
import info.joriki.io.Readable;
import info.joriki.io.ReadableBufferedInputStream;
import info.joriki.io.UncheckedInflaterInputStream;
import info.joriki.io.ManuallyCheckedInflaterInputStream;

import info.joriki.pdf.PDFDictionary;

public class FlateDecoder extends PredictingFilter
{
  public Readable getPredictable (Readable raw,PDFDictionary parameters)
  {
    InputStream in = Util.toInputStream (raw);
    try {
      return new ReadableBufferedInputStream
      (parameters != null && parameters.get ("TreadBytely") != null ?
          (InputStream) new ManuallyCheckedInflaterInputStream (in,1) :
          (InputStream) new UncheckedInflaterInputStream (in));
    } catch (ZipException ze) {
      ze.printStackTrace ();
      return null;
    }
  }
}
