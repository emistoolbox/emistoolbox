/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import java.io.IOException;
import java.io.InputStream;

/**
 * An input stream concatenation concatenates any number of input streams
 * to form a single stream.
 */
public class InputStreamConcatenation extends ReadableInputStream
{
  int delimiter;
  Iterator inputStreams;
  InputStream currentStream;
  
  /**
   * Creates an input stream concatenation from the input streams
   * specified by an enumeration.
   * @param inputStreams an enumeration enumerating the input streams
   * to be concatenated
   */
  public InputStreamConcatenation (Iterator inputStreams)
  {
    this (inputStreams,-1);
  }

  public InputStreamConcatenation (Iterator inputStreams,int delimiter)
  {
    this.delimiter = delimiter;
    this.inputStreams = inputStreams;
    currentStream = inputStreams.hasNext () ? 
      (InputStream) inputStreams.next () :
      new EmptyInputStream ();
  }
  
  public InputStreamConcatenation (InputStream [] inputStreams)
  {
    this (Arrays.asList (inputStreams).iterator ());
  }

  public InputStreamConcatenation (InputStream in1,InputStream in2)
  {
    this (new InputStream [] {in1,in2});
  }

  final void nextStream ()
  {
    currentStream = (InputStream) inputStreams.next ();
  }

  /**
   * Reads a single byte, switching to the next input stream if
   * the end of the current one is reached.
   * @return the next byte of data, or <code>-1</code> if the end
   * of the stream has been reached
   * @exception IOException if an I/O error occurs
   */
  public int read () throws IOException
  {
    for (;;)
      {
        int b = currentStream.read ();
        if (b != -1)
          return b;
        try {
          nextStream ();
          if (delimiter != -1)
            return delimiter;
        } catch (NoSuchElementException nsee) {
          return -1;
        }
      }
  }

  /**
   * Reads the specified section of the specified byte array, switching
   * to the next input stream whenever the end of the current one is
   * reached.
   * @return the number of bytes actually read, or <code>-1</code> if
   * there is no more data because the end of the stream has been reached
   * @param b the array into which bytes are to be read
   * @param off the offset of the bytes in the array
   * @param len the number of bytes to be read
   * @exception IOException if an I/O error occurs
   */
  public int read (byte [] b,int off,int len) throws IOException
  {
    int got = 0;

    do
      {
        int read = currentStream.read (b,off + got,len - got);
        if (read == -1)
          try {
            nextStream ();
            if (delimiter != -1)
              b [off + got++] = (byte) delimiter;
          } catch (NoSuchElementException nsee) {
            return got == 0 ? -1 : got;
          }
        else
          got += read;
      }
    while (got != len);

    return got;
  }
}
