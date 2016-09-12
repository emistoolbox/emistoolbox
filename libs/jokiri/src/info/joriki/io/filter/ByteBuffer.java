/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io.filter;

import java.io.IOException;

import info.joriki.io.Readable;
import info.joriki.io.Writeable;

/**
   A byte buffer buffers bytes. It acts both as a byte source and as
   a byte sink. It will expand to arbitrary size to buffer any
   bytes that have been written but not read.
*/

public class ByteBuffer extends AtomicBuffer implements Readable, Writeable
{
  // a circular buffer for data that hasn't been requested yet
  // it's private to make sure that buf == tmp is never true
  // if buf has been set by a read request
  private byte [] tmp;

  // where data is currently being written to, viz.:
  // if a call to read is being served:
  //    the buffer passed to that call;
  // if there is no unsatisfied read request:
  //    tmp
  byte [] buf;

  // the size of the section in buf being written to
  int size;

  // the first index past the section in buf being written to
  int lim;

  // the first index of data in buf
  int beg = 0;

  // the next index in buf to be written to 
  int end = 0;

  // inherited from AtomicBuffer:
  // the number of bytes currently in buf
  // protected int len = 0;

  /**
     Constructs a new byte buffer with an initial capacity of a single byte.
  */
  public ByteBuffer ()
  {
    this (1);
  }

  /**
     Constructs a new byte buffer with the given initial capacity.
     @param n the initial capacity of the buffer
  */
  public ByteBuffer (int n)
  {
    buf = tmp = new byte [n];
    lim = size = n;
  }
 
  final void setBuffer (byte [] b)
  {
    setBuffer (b,0,b.length);
  }

  // make the specified section of b the current buffer
  void setBuffer (byte [] b,int off,int n)
  {
    // if we already have some data, copy it to the beginning
    // of the specified section.
    if (len != 0)
      {
        if (beg >= end)
          {
            // the hard case: the circular buffer has wrapped around
            int firstLot = lim - beg;
            System.arraycopy (buf,beg,b,off,firstLot);
            System.arraycopy (buf,0,b,off + firstLot,end);
          }
        else
          // the easy case
          System.arraycopy (buf,beg,b,off,len);
      }

    buf = b;
    beg = off;
    end = beg + len;
    lim = beg + n;
    size = n;
  }

  /**
     Writes a single byte to this buffer and then cranks the
     output crank if there is one.
     @param b the byte to be written
     @exception IOException if the output crank throws an
     <code>IOException</code>
  */
  public void write (int b) throws IOException
  {
    // is the buffer full?
    if (len == size)
      // this isn't executed if buf != tmp since in that
      // case len == size has already triggered a switchBack
      require (1);
    buf [end++] = (byte) b;
    // has it wrapped around?
    if (end == lim && buf == tmp)
      end = 0;
    len++;
    // check whether a read request has been satisfied
    maybeSwitchBack ();
    // push data down the chain if necessary
    outputCrank ();
  }

  /**
     Writes the entire contents of the specified byte array to
     this buffer and then cranks the output crank if there is one.
     @param b the array of bytes to be written to this buffer
     @exception IOException if the output crank throws an
     <code>IOException</code>
  */
  public void write (byte [] b) throws IOException
  {
    write (b,0,b.length);
  }

  /**
     Writes the specified section of the specified byte array to
     this buffer and then cranks the output crank if there is one.
     @param b the array containing the bytes to be written
     @param off the offset of the bytes in the array
     @param n the number of bytes to be written
     @exception IOException if the output crank throws an
     <code>IOException</code>
  */
  public void write (byte [] b,int off,int n) throws IOException
  {
    int left = size - len;
    if (n > left)
      {
        // more data arrived than we have space for in buf
        // if we're processing a read request, 
        if (buf != tmp)
          {
            // fill the rest of the read buffer
            write (b,off,left);
            // switch back to the temporary buffer
            switchBack ();
            // then proceed as normal with the remaining data
            off += left;
            n -= left;
          }
        // in any case, now buf == tmp,
        // make sure we have space to take n bytes on board.
        require (n);
      }

    left = lim - end;
    if (n >= left)
      {
        // the hard case : the new data makes the buffer wrap around
        System.arraycopy (b,off,buf,end,left);
        end = n - left;
        System.arraycopy (b,off + left,buf,0,end);
      }
    else
      {
        // the easy case
        System.arraycopy (b,off,buf,end,n);
        end += n;
      }
    len += n;
    
    // check whether a read request has been satisfied
    maybeSwitchBack ();
    // push data down the chain if necessary
    outputCrank ();
  }

  /**
     Reads a single byte from this buffer, cranking the input
     crank if necessary.
     @return
     <table><tr valign=top><td>
     one byte
     </td><td>
     if it was available or could be provided by the input crank
     </td></tr><tr valign=top><td>
     {@link Filter#EOI EOI}
     </td><td>
     if the input crank was not able to provide the requested byte
     </td></tr><tr valign=top><td>
     {@link Filter#EOD EOD}
     </td><td>
     if the requested byte is not available and
     this buffer has no input crank to crank
     </td></tr></table>
     @exception IOException if the input crank throws an
     <code>IOException</code>
  */
  public int read () throws IOException
  {
    // generate data if necessary
    int eod = inputCrank (1);
    // pass any "end of" code on down the chain
    if (eod != OK)
      return eod;
    len--;
    int result = buf [beg++] & 0xff;
    // has the buffer wrapped around?
    if (beg == lim)
      beg = 0;
    return result;
  }

  /**
     Reads bytes from this buffer to the specified array, cranking
     the input crank if necessary.
     @param b the array into which the bytes are to be read
     @return
     <table><tr valign=top><td>
     the number of bytes read
     </td><td>
     if any bytes are available or could be provided by the input crank
     </td></tr><tr valign=top><td>
     {@link Filter#EOI EOI}
     </td><td>
     if no bytes are available and the input crank was not
     able to provide any bytes at all
     </td></tr><tr valign=top><td>
     {@link Filter#EOD EOD}
     </td><td>
     if the requested number of bytes is not available and
     this buffer has no input crank to crank
     </td></tr></table>
     @exception IOException if the input crank throws an
     <code>IOException</code>
  */
  public int read (byte [] b) throws IOException
  {
    return read (b,0,b.length);
  }

  /**
     Reads bytes from this buffer to the specified section
     of the specified array, cranking the input crank if necessary.
     @param b the array into which the bytes are to be read
     @param off the offset of the bytes in the array
     @param n the number of bytes to be read
     @return
     <table><tr valign=top><td>
     the number of bytes read
     </td><td>
     if any bytes are available or could be provided by the input crank
     </td></tr><tr valign=top><td>
     {@link Filter#EOI EOI}
     </td><td>
     if no bytes are available and the input crank was not
     able to provide any bytes at all
     </td></tr><tr valign=top><td>
     {@link Filter#EOD EOD}
     </td><td>
     if the requested number of bytes is not available and
     this buffer has no input crank to crank
     </td></tr></table>
     @exception IOException if the input crank throws an
     <code>IOException</code>
  */
  public int read (byte [] b,int off,int n) throws IOException
  {
    if (len < n)
      {
        // there is not enough data to satisfy the request.
        // if there is no input crank, that's OK, just return EOD
        if (inputCrank == null)
          return EOD;

        // if there is one, try to generate data on the input side
        int got = len;
        if (len != 0)
          // a recursive call that deals with the part of the
          // request we can already satisfy
          read (b,off,len);
        // now set the buffer to the section that needs to be filled,
        // so any data is written directly to b and doesn't need to
        // be copied unnecessarily
        setBuffer (b,off + got,n - got);
        // generate more data
        while (buf == b && inputCrank.crank () == OK)
          ;
        if (buf == b)
          {
            // we couldn't get enough data to satisfy the request.
            // count up how much we did get
            got += len;
            // switch back to the temporary buffer
            switchBack ();
            // we have an input crank, so we should return either
            // the number of bytes actually read or EOI.
            return got == 0 ? EOI : got;
          }
        // we got all the data; fall through to return n
      }
    else
      {
        // there is enough data to satisfy the request.
        int left = lim - beg;
        if (n >= left)
          {
            // the hard case : the data to be read wraps around
            System.arraycopy (buf,beg,b,off,left);
            beg = n - left;
            System.arraycopy (buf,0,b,off + left,beg);
          }
        else
          {
            // the easy case
            System.arraycopy (buf,beg,b,off,n);
            beg += n;
          }
        len -= n;
      }
    return n;
  }

  /**
     Returns the number of bytes currently in the buffer.
     @return the number of bytes currently in the buffer
  */
  public int available ()
  {
    return len;
  }

  // make sure there's space in tmp to take n more bytes on board
  // only to be called when buf == tmp.
  private void require (int n)
  {
    int newSize = tmp.length;
    // double buffer size as long as necessary
    while (newSize < len + n)
      newSize <<= 1;
    
    tmp = new byte [newSize];
    setBuffer (tmp);
  }

  // switch back to the temporary buffer if we've satisfied the read request
  private void maybeSwitchBack ()
  {
    if (buf != tmp && len == size)
      switchBack ();
  }

  // switch back to the temporary buffer
  private void switchBack ()
  {
    len = 0;
    setBuffer (tmp);
  }
}
