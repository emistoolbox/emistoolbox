/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.util;

public class Switch extends Option {
  final static Object dummyObject = new Object ();

  public Switch (String description)
  {
    super (description);
  }
  
  public void set ()
  {
    value.set (dummyObject);
  }
}
