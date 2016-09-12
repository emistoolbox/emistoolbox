/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.util;

public class AssertionException extends RuntimeException
{
  public AssertionException () {}
  public AssertionException (String message) { super (message); }
}
