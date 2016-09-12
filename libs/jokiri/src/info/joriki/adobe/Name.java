/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.adobe;

public class Name
{
  public String name;

  public Name (String name)
  {
    this.name = name;
  }

  public String toString ()
  {
    return "/" + name;
  }

  public boolean equals (Object o)
  {
    return o instanceof Name && ((Name) o).name.equals (name);
  }
}
