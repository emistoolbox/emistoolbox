/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.crypto;

public interface Digest
{
  void digest (int b);
  void digest (int b,int n);
  void digest (byte [] b);
  void digest (byte [] b,int off,int len);
  void reset  ();
  byte [] getDigest ();
}
