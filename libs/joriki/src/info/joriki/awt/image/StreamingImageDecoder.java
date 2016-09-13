/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import info.joriki.io.ByteArrayInputStreamProvider;
import info.joriki.io.InputStreamProvider;
import info.joriki.io.FileInputStreamProvider;
import info.joriki.io.InputStreamInputStreamProvider;

abstract public class StreamingImageDecoder extends StaticImageDecoder
{
  protected InputStreamProvider provider;
  protected InputStream in;

  public StreamingImageDecoder () {}

  public StreamingImageDecoder (int hints)
  {
    super (hints);
  }

  public void setSource (Object source)
  {
    if (source instanceof InputStream)
      provider = new InputStreamInputStreamProvider ((InputStream) source);
    else if (source instanceof File)
      provider = new FileInputStreamProvider ((File) source);
    else if (source instanceof String)
      provider = new FileInputStreamProvider ((String) source);
    else if (source instanceof byte [])
      provider = new ByteArrayInputStreamProvider ((byte []) source);
    else
      throw new Error ("unknown source " + source.getClass ());
  }

  protected void produceImage () throws IOException
  {
    if (provider != null)
      in = provider.getInputStream ();
    super.produceImage ();
  }
}
