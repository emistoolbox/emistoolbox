/*
 * Copyright 2007 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

public interface PairHandler {
  boolean handle (PDFObject objectA,PDFObject objectB);
}
