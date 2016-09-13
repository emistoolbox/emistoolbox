/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.util;

import java.io.IOException;

public interface OptionHandler
{
  boolean handle (char option,ArgumentIterator args) throws IOException;
}
