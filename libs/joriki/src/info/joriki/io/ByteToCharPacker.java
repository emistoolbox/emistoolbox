/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

public class ByteToCharPacker extends ByteArrayCharacterSource
{
  public ByteToCharPacker (byte [] arr)
  {
    super (arr);
  }

  public int read ()
  {
    if (pos >= arr.length)
      return -1;
    int code = (arr [pos++] & 0xff) << 8;
    if (pos >= arr.length)
      return -1;
    return code | (arr [pos++] & 0xff);
  }
}
