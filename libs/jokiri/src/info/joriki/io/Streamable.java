/*
 * Copyright 2006 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.OutputStream;
import java.io.IOException;

public interface Streamable {
  void writeTo (OutputStream out) throws IOException;
}
