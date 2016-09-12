/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.util;

public class StringParameter extends FermiParameter {
  public StringParameter (String defaultValue,String description)
  {
    super (defaultValue,description);
  }
  
  public void set(String string) {
    value.set (string);
  }

  public String get () {
    return (String) getValue ();
  }
  
  protected String getParameterName() {
    return "s";
  }
}
