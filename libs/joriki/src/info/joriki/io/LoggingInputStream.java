/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LoggingInputStream extends ReadableFilterInputStream
{
  OutputStream log;
  boolean logging;
  
  public LoggingInputStream (InputStream in,OutputStream log)
  {
    this (in,log,true);
  }

  public LoggingInputStream (InputStream in,OutputStream log,boolean logging)
  {
    super (in);
    this.log = log;
    setLogging (logging);
  }

  public int read () throws IOException
  {
    int b = in.read ();
    if (logging && b >= 0)
      log.write (b);
    return b;
  }

  public int read (byte [] buf,int off,int len) throws IOException
  {
    int n = in.read (buf,off,len);
    if (logging && n > 0)
      log.write (buf,off,n);
    return n;
  }

  public void setLogging (boolean logging)
  {
    this.logging = logging;
  }
}
