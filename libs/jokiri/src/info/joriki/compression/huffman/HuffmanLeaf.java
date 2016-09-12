/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.compression.huffman;

import info.joriki.util.CloneableObject;

class HuffmanLeaf extends CloneableObject
{
  int code;
  int codeLength;
  int symbol;
}
