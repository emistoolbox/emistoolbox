/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;

import info.joriki.io.Streamable;

public class ByteArray implements Streamable, Iterable<Byte>
{
  public byte [] arr;
  public int beg;
  public int end;

  public ByteArray (byte [] arr)
  {
    this.arr = arr;
    this.beg = 0;
    this.end = arr.length;
  }

  public ByteArray (byte [] arr,int beg,int end)
  {
    this.arr = arr;
    this.beg = beg;
    this.end = end;
  }

  public ByteArray (ByteArrayOutputStream baos)
  {
    this (baos.toByteArray ());
  }

  public int hashCode ()
  {
    int res = 0;
    for (int i = beg,shift = 0;i < end;i++,shift += 8)
      res ^= arr [i] << shift;
    return res;
  }

  public boolean equals (Object o)
  {
    if (!(o instanceof ByteArray))
      return false;

    ByteArray b = (ByteArray) o;
    if (b.end - b.beg != end - beg)
      return false;

    for (int i = beg;i < end;i++)
      if (arr [i] != b.arr [i + b.beg - beg])
        return false;
    
    return true;
  }

  public String toString ()
  {
    return new String (arr,beg,end - beg);
  }

  public ByteArrayInputStream getInputStream ()
  {
    return new ByteArrayInputStream (arr,beg,end - beg);
  }

  public void writeTo (ByteArrayOutputStream out) // to avoid IOException
  {
    out.write (arr,beg,end - beg);
  }

  public void writeTo (OutputStream out) throws IOException
  {
    out.write (arr,beg,end - beg);
  }

  public int length ()
  {
    return end - beg;
  }

  public byte [] toByteArray ()
  {
    byte [] result = new byte [end - beg];
    System.arraycopy (arr,beg,result,0,result.length);
    return result;
  }

  public Iterator<Byte> iterator () {
    return new Iterator<Byte> () {
      int pos = beg;
      public boolean hasNext () {
        return pos < end;
      }

      public Byte next () {
        return arr [pos++];
      }

      public void remove () {
        throw new UnsupportedOperationException ();
      }
    };
  }
}
