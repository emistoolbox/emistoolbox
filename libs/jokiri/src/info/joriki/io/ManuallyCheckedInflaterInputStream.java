/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.EOFException;
import java.io.DataInputStream;

import java.util.zip.Adler32;
import java.util.zip.ZipException;
import java.util.zip.CheckedInputStream;

public class ManuallyCheckedInflaterInputStream
  extends CheckedInputStream
  implements Readable
{
  InputStream raw;

  public ManuallyCheckedInflaterInputStream (InputStream raw,int size)
    throws ZipException
  {
    super (new UncheckedInflaterInputStream (raw,size,false),new Adler32 ());
    this.raw = raw;
  }

  public int read (byte [] buf,int off,int len) throws IOException
  {
    init ();
    return check (super.read (buf,off,len));
  }

  public int read (byte [] buf) throws IOException
  {
    init ();
    return check (super.read (buf));
  }

  public int read () throws IOException
  {
    init ();
    return check (super.read ());
  }

  // We need to give RewritingContentStreamHandler.rewriteInlineImage
  // a chance to set up logging before we read the header
  boolean initialized;
  private void init () throws ZipException {
    if (!initialized) {
      ((UncheckedInflaterInputStream) in).init ();
      initialized = true;
    }
  }
  
  boolean done;
  private int check (int result) throws IOException
  {
    if (result == -1 && !done)
      {
        done = true;
        try {
          int maybeChecksum = new DataInputStream (raw).readInt ();
          if (maybeChecksum != (int) getChecksum ().getValue ())
            throw new IncorrectChecksumException (maybeChecksum);
        } catch (EOFException eofe) {}
      }
    return result;
  }
}
