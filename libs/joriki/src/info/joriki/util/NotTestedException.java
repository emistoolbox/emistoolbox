/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.util;

public class NotTestedException extends RuntimeException
{
  public NotTestedException () {}
  public NotTestedException (String message) { super (message + " not tested"); }
}
