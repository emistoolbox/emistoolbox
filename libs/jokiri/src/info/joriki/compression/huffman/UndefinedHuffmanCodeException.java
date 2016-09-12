/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.compression.huffman;

import java.io.StreamCorruptedException;

public class UndefinedHuffmanCodeException extends StreamCorruptedException
{
  public int nextBits;

  public UndefinedHuffmanCodeException (int nextBits)
  {
    super ("Undefined Huffman code");
    this.nextBits = nextBits;
  }
}
