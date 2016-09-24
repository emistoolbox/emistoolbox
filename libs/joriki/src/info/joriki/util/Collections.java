/*
 * Copyright 2006 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.util;

import java.util.Iterator;

public class Collections {
  private Collections () {}
  
  public static <T> T next (Iterator<T> iterator) {
    return iterator.hasNext () ? iterator.next () : null;
  }
}
