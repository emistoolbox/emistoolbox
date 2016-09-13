/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.util;

public abstract class Parameter extends Option {
  Parameter (String description) {
    super (description);
  }
  protected abstract String getParameterName ();
  public abstract void set (String string);
}
