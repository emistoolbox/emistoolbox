/*
 * Copyright 2006 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.IOException;

abstract public class AbstractInputStreamProvider implements InputStreamProvider {
  public byte [] toByteArray () throws IOException {
    return Util.toByteArray (getInputStream ());
  }
}
