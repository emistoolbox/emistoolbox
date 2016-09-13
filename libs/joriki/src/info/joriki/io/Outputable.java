/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.DataOutput;
import java.io.IOException;

public interface Outputable
{
  void writeTo (DataOutput out) throws IOException;
}
