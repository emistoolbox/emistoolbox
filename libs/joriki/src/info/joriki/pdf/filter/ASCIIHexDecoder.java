/*
 * Copyright 2005 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf.filter;

import info.joriki.compression.ascii.Base16Decoder;

public class ASCIIHexDecoder extends ASCIIFilter
{
  public ASCIIHexDecoder () {
    super (new Base16Decoder ());
  }
}
