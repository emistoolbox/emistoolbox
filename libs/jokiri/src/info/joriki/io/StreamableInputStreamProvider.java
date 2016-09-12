/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.InputStream;

public class StreamableInputStreamProvider implements InputStreamProvider
{
  Streamable streamable;

  public StreamableInputStreamProvider (Streamable streamable)
  {
    this.streamable = streamable;
  }

  public byte [] toByteArray ()
  {
    return Util.toByteArray (streamable);
  }

  public InputStream getInputStream ()
  {
    return new ReadableByteArray (toByteArray ());
  }
}
