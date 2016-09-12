/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.util;

abstract public class Option {
  final String description;
  protected ThreadLocal value = new ThreadLocal ();
  
  Option (String description)
  {
    this.description = description;
  }
  
  public void reset ()
  {
    value.set (null);
  }

  public boolean isSet ()
  {
    return value.get () != null;
  }
}
