/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.IOException;
import java.io.InputStream;

import java.util.zip.ZipException;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import info.joriki.util.Options;

public class UncheckedInflaterInputStream
  extends InflaterInputStream
  implements Readable
{
  public UncheckedInflaterInputStream (InputStream in) throws ZipException
  {
    super (in,new Inflater (true));
    init ();
  }

  public UncheckedInflaterInputStream (InputStream in,int size,boolean initialize) throws ZipException
  {
    super (in,new Inflater (true),size);
    if (initialize)
      init ();
  }

  final static int MAX_WBITS = 15; // from zconf.h
  final static int PRESET_DICT = 0x20;// from zutil.h

  // this is modelled on inflate.c
  public void init () throws ZipException
  {
    try {
      int method = in.read ();
      // a hack, necessary because bt1585747580.pdf contains an
      // extra 0xa character before the stream data
      if (method == 0xa)
        {
          Options.warn ("extra newline character before compressed data");
          method = in.read ();
        }
      if (method < 0)
        throw new ZipException ("zip stream ended before it had even begun");
      if ((method & 0xf) != 8)
        throw new ZipException ("unknown compression method " + (method & 0xf));
      if ((method >> 4) + 8 > MAX_WBITS)
        throw new ZipException ("invalid window size");
      int flags = in.read ();
      if (flags < 0)
        throw new ZipException ("zip stream ended before it had even begun");
      if ((((method << 8) | flags) % 31) != 0)
        throw new ZipException ("incorrect header check");
      if ((flags & PRESET_DICT) != 0)
        throw new ZipException ("zip stream needs preset dictionary");
    } catch (IOException ioe) {
      if (ioe instanceof ZipException)
        throw (ZipException) ioe;
      ioe.printStackTrace ();
      throw new ZipException ("I/O error while initializing zip input stream");
    }
  }
}
