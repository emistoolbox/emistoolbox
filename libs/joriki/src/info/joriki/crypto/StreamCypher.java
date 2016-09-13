/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.crypto;

public interface StreamCypher
{
  byte encrypt (int b);
  byte decrypt (int b);
  void reset ();
}
