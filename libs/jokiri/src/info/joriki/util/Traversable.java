/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.util;

public interface Traversable<T>
{
  public void traverse (Handler<T> handler);
}
