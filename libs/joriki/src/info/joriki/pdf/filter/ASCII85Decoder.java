/*
 * Copyright 2003 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf.filter;

import info.joriki.compression.ascii.Base85Decoder;

public class ASCII85Decoder extends ASCIIFilter
{
  public ASCII85Decoder () {
    super (new Base85Decoder ());
  }
}
