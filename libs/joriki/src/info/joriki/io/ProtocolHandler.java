/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.IOException;
import java.net.URL;

public interface ProtocolHandler {
  FullySeekable getFullySeekable (URL url) throws IOException;
}
