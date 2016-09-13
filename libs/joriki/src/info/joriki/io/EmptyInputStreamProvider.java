/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.InputStream;

public class EmptyInputStreamProvider extends AbstractInputStreamProvider
{
  public InputStream getInputStream ()
  {
    return new EmptyInputStream ();
  }
}
