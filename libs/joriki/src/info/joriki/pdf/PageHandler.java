/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.io.IOException;

public interface PageHandler
{
  boolean handle (PageInfo info) throws IOException;
  void finish () throws IOException;
}
