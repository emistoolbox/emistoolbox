/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io.filter;

import java.io.InputStream;

import info.joriki.io.ByteSource;

/**
 * A chameleon source recipient can take either a byte source
 * or an input stream as a source.
 */
public class ChameleonSourceRecipient implements SourceRecipient
{
  boolean fromStream;
  ByteSource byteSource;
  InputStream inputStream;

  /**
     Sets the source that this converter reads from.
     The source must either be an input stream or implement
     the <code>ByteSource</code> interface.
     @param source the source to be read from
  */
  public void setSource (Object source)
  {
    fromStream = source instanceof InputStream;
    if (fromStream)
      inputStream = (InputStream) source;
    else
      byteSource = (ByteSource) source;
  }
}
