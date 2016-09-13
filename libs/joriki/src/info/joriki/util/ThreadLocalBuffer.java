/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.util;

// I did timing tests; getBuffer () is very slightly
// slower than fresh allocation for a 0-byte buffer,
// but about 100 times faster for a 2048-byte buffer.
public class ThreadLocalBuffer extends ThreadLocal
{
  int size;
  
  public ThreadLocalBuffer (int size)
  {
    this.size = size;
  }

  protected Object initialValue ()
  {
    return new byte [size];
  }
  
  public byte [] getBuffer ()
  {
    return (byte []) get ();
  }
}
