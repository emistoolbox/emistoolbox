/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.crypto;

abstract public class SymmetricStreamCypher implements StreamCypher
{
  public final byte encrypt (int b) { return crypt (b); }
  public final byte decrypt (int b) { return crypt (b); }
  abstract protected byte crypt (int b);
}
