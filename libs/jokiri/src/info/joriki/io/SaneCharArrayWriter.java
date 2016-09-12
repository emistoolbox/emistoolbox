/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import info.joriki.util.Unicode;

import java.io.CharArrayWriter;

public class SaneCharArrayWriter extends CharArrayWriter
{
  public SaneCharArrayWriter () {}

  public SaneCharArrayWriter (int initialSize)
  {
    super (initialSize);
  }

  public void write (String str) // throws no IOException
  {
    write (str,0,str.length ());
  }

  public void write (char [] arr) // throws no IOException
  {
    write (arr,0,arr.length);
  }
  
  public void write (int c) // treats 3-byte characters properly
  {
    if (c < 0x10000)
      super.write (c);
    else
      write (Unicode.toChars (c));
  }
}
