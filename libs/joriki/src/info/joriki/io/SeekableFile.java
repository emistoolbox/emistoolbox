/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import java.util.Stack;

/**
 * A seekable file is a random access file extended to implement the interface
 * <code>FullySeekable</code>.
 */
public class SeekableFile extends RandomAccessFile implements FullySeekableDataOutput, FullySeekableDataInput
{
  private Stack offsetStack = new Stack ();
  long offset = 0;
  
  /**
   * Constructs a seekable file to read from, and optionally 
   * to write to, a file with the specified name. 
   * <p>
   * The mode argument must either be equal to <code>"r"</code> or 
   * <code>"rw"</code>, indicating either to open the file for input or 
   * for both input and output. 
   *
   * @param      name   the file name
   * @param      mode   the access mode
   * @exception  IllegalArgumentException  if the mode argument is not equal
   *               to <code>"r"</code> or to <code>"rw"</code>
   * @exception  IOException               if an I/O error occurs
   */
  public SeekableFile (String name,String mode) throws IOException
  {
    super (name,mode);
  }

  /**
   * Constructs a seekable file to read from, and optionally 
   * to write to, the file specified by the <code>File</code> argument. 
   * <p>
   * The mode argument must either be equal to <code>"r"</code> or to 
   * <code>"rw"</code>, indicating either to open the file for input, 
   * or for both input and output, respectively. 
   *
   * @param      file   the file object
   * @param      mode   the access mode
   * @exception  IllegalArgumentException  if the mode argument is not equal
   *               to <code>"r"</code> or to <code>"rw"</code>
   * @exception  IOException               if an I/O error occurs
   */
  public SeekableFile (File file,String mode) throws IOException
  {
    super (file,mode);
  }

  /**
   * Returns the number of bytes in the file after the current position.
   * @return the number of bytes in the file after the current position
   * @exception IOException if an I/O error occurs
   */
  public int available () throws IOException
  {
    return (int) (length () - getFilePointer ());
  }

  /**
   * Sets the file-pointer offset relative to the current offset,
   * as determined by the <code>getFilePointer</code> method.
   * In contrast to <code>skipBytes</code>, this method
   * always moves the current position exactly by the specified
   * number of bytes and allows movement by negative offsets.
   * @param delta the distance from the current offset
   * @see #seek(long)
   * @see #getFilePointer()
   */
  public void seekRelative (long delta) throws IOException
  {
    seek (getFilePointer () + delta);
  }

  /**
   * Sets the file-pointer offset relative to the end of the file,
   * as determined by the <code>length</code> method.
   * @param delta the distance from the end of the file
   * @see java.io.RandomAccessFile#seek(long)
   * @see java.io.RandomAccessFile#length()
   */
  public void seekFromEnd (long delta) throws IOException
  {
    seek (length () + delta);
  }

  // for Markable

  Stack markStack = new Stack ();

  public void mark (int readLimit) throws IOException
  {
    mark ();
  }

  public void mark () throws IOException
  {
    markStack.push (new Long (super.getFilePointer ()));
  }

  public void reset () throws IOException
  {
    super.seek (((Long) markStack.pop ()).longValue ());
  }

  public void seek (long pos) throws IOException
  {
    super.seek (pos + offset);
  }

  public long getFilePointer () throws IOException
  {
    return super.getFilePointer () - offset;
  }

  public void pushOffset (long pos) throws IOException
  {
    offsetStack.push (new Long (offset));
    mark ();
    seek (pos);
    offset = super.getFilePointer ();
  }

  public void popOffset () throws IOException
  {
    offset = ((Long) offsetStack.pop ()).intValue ();
    reset ();
  }

  public long getOffset ()
  {
    return offset;
  }
}
