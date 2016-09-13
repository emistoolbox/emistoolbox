/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.InputStream;

public class ByteArrayInputStreamProvider implements InputStreamProvider
{
  byte [] arr;

  public ByteArrayInputStreamProvider (byte [] arr)
  {
    this.arr = arr;
  }

  public InputStream getInputStream ()
  {
    return new ReadableByteArray (arr);
  }

  public byte [] toByteArray ()
  {
    return arr;
  }
}
