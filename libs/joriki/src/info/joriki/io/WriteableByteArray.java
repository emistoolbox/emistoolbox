/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

/**
 * A writeable byte array is a byte array output stream
 * made to implement the interface <code>Writeable</code>.
 */
public class WriteableByteArray extends java.io.ByteArrayOutputStream implements Writeable {
  // no IOException thrown
  public void write (byte [] b) {
    write (b,0,b.length);
  }
}
