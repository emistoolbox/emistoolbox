/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.util;

public class NotImplementedException extends RuntimeException
{
  public NotImplementedException () {}
  public NotImplementedException (String message) { super (message + " not implemented"); }
}
