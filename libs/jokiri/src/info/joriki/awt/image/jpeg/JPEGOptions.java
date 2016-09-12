/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.jpeg;

import info.joriki.util.Switch;

public interface JPEGOptions {
  Switch defaultHuffmanCodes = new Switch ("don't optimize JPEG Huffman codes");
}
