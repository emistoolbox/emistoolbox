/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.StreamCorruptedException;

public class KnownStreamCorruptedException extends StreamCorruptedException
{
  public KnownStreamCorruptedException () {}
  public KnownStreamCorruptedException (String message) { super (message); }
}
