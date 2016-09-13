/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

/**
 * An empty input stream is an input stream with no data to be read.
 */
public class EmptyInputStream extends ReadableInputStream
{
  /**
     Always signals end of file.
     @return <code>-1</code>
  */
  public int read () { return -1; }
}
