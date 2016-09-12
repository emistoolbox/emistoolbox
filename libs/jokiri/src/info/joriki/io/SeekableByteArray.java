/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.IOException;
import java.io.EOFException;
import java.io.DataInputStream;

import java.util.Stack;

/**
 * A seekable byte array is essentially a byte array turned into a
 * random access file, with all the seeking functionality specified
 * by the <code>FullySeekable</code> interface.
 */
public class SeekableByteArray extends DataInputStream implements FullySeekableDataInput
{
  /**
   * The underlying byte array input stream
   */
  protected SimpleByteArrayInputStream bais;

  /**
   * The start index of the data in the byte array
   */
  protected int start;
  /**
   * The offset relative to which the current position is measured
   */
  protected int offset;
  /**
   * The length of the data
   */
  protected int length;

  private Stack offsetStack = new Stack ();
  
  /**
   * Constructs a seekable byte array from the entire byte array specified.
   * @param arr The byte array to be used
   */
  public SeekableByteArray (byte [] arr)
  {
    this (arr,0,arr.length);
  }

  /**
   * Constructs a seekable byte array from the given section of a byte array.
   @param arr the byte array to be used
   @param beg the start index of the data
   @param len the length of the data
   */
  public SeekableByteArray (byte [] arr,int beg,int len)
  {
    super (new SimpleByteArrayInputStream (arr,beg,len));
    bais = (SimpleByteArrayInputStream) in;
    start = offset = beg;
    length = len;
  }
  
  /**
   * Constructs a seekable byte array from the specified seekable byte array.
   *
   * The new seekable byte array begins at the current position of the
   * specified one and extends to its end.
   * @param sba the seekable byte array to be used
   */
  public SeekableByteArray (SeekableByteArray sba)
  {
    this (sba,sba.bais.available ());
  }

  /**
   * Constructs a seekable byte array from the specified seekable byte array.
   *
   * The new seekable byte array begins at the current position of the
   * specified one and has the length specified.
   * @param sba the seekable byte array to be used
   * @param len the length of the data
   */
  public SeekableByteArray (SeekableByteArray sba,int len)
  {
    this (sba.bais.buf,sba.bais.pos,len);
  }

  private final void setPosition (int pos)
  {
    bais.pos = pos;
  }

  /**
   * Repositions the seekable byte array at the beginning of the data.
   *
   * Note that this is the actual beginning of the data as specified to
   * the constructor, not position 0 relative to any offset specified
   * using <code>setOffset</code>.
   */
  public void rewind ()
  {
    setPosition (start);
  }

  public void pushOffset (long pos)
  {
    offsetStack.push (new Integer (offset));
    mark ();
    seek (pos);
    offset = bais.pos;
  }

  public void popOffset () throws IOException
  {
    offset = ((Integer) offsetStack.pop ()).intValue ();
    reset ();
  }
  
  public long getOffset ()
  {
    return offset;
  }
  
  /**
   * Repositions the seekable byte array to the specified position
   * relative to the offset set using <code>setOffset</code>.
   * @param where the position to be sought
   */
  public void seek (long where)
  {
    setPosition ((int) where + offset);
  }

  /**
   * Returns the current position
   * relative to the offset set using <code>setOffset</code>.
   * @return the current position
   */
  public long getFilePointer ()
  {
    return bais.pos - offset;
  }

  /**
   * Sets the position relative to the current position.
   * @param delta the distance from the current position
   */
  public void seekRelative (long delta)
  {
    bais.pos += delta;
  }

  /**
   * Returns the first position beyond the end of data. This
   * is equal to the total number of bytes only if the offset has not
   * been set to a non-zero value.
   * @return the first position beyond the end of data
   * @see #setOffset
   */
  public long length ()
  {
    return bais.count - offset;
  }
  /**
   * Sets the position relative to the end,
   * as determined by the <code>length</code> method.
   * @param delta the distance from the end of the file
   */
  public void seekFromEnd (long delta)
  {
    setPosition ((int) (length () + delta));
  }

  public void write (int b) throws EOFException
  {
    bais.write (b);
  }

  public void write (byte [] b) throws EOFException
  {
    bais.write (b);
  }

  public void write (byte [] b,int off,int len) throws EOFException
  {
    bais.write (b,off,len);
  }

  public void writeTo (Writeable out,int n) throws IOException
  {
    bais.writeTo (out,n);
  }
  
  // this isn't part of Seekable
  /**
   * Peeks at the byte at the current position.
   * @return the byte at the current position, or <code>-1</code> if the
   * end of the seekable byte array has been reached
   */
  public int peek ()
  {
    return bais.peek ();
  }

  /**
   * Returns a string constructed from the entire underlying data.
   * @return a string constructed from the entire underlying data
   */
  public String toString ()
  {
    return new String (bais.buf,start,length);
  }

  public void mark ()
  {
    bais.mark ();
  }
}
