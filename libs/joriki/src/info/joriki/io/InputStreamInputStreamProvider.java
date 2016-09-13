/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.InputStream;

/**
 * This class wraps an input stream so as to make it accessible
 * through the <code>InputStreamProvider</code> interface.
 * Unlike "proper" input stream providers, this class does not
 * provide multiple identical copies of an input stream.
 */

public class InputStreamInputStreamProvider extends AbstractInputStreamProvider
{
  InputStream inputStream;

  /**
   * Creates an <code>InputStreamInputStreamProvider</code> that
   * provides the specified input stream.
   */
  public InputStreamInputStreamProvider (InputStream inputStream)
  {
    this.inputStream = inputStream;
  }

  /**
   * Provides the input stream associated with this input stream provider
   * on the first call and returns <code>null</code> on all further calls.
   * @return the input stream associated with this input stream provider,
   * or <code>null</code> if this method has been called before
   */
  public InputStream getInputStream ()
  {
    InputStream result = inputStream;
    inputStream = null;
    return result;
  }
}
