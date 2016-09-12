/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.charstring;

import info.joriki.io.ByteStream;

public interface CharStringSpeaker
{
  void command (int c,ByteStream byteStream);
  void escape (int e);
  void argument (double a);
}
