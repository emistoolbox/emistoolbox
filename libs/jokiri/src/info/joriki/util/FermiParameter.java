/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.util;

abstract public class FermiParameter extends Parameter {
  Object defaultValue;
  
  FermiParameter (Object defaultValue,String description)
  {
    super (description);
    this.defaultValue = defaultValue;
  }
  
  protected Object getValue()
  {
    Object localValue = value.get ();
    return localValue != null ? localValue : defaultValue;
  }
}
