/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.util;

import java.util.Stack;

public class DebugStack extends Stack
{
  int max = 0;
  String name;

  public DebugStack (String name)
  {
    this.name = name;
  }

  public Object push (Object o)
  {
    super.push (o);
    if (elementCount > max)
      {
        max = elementCount;
        System.out.println (name + " : " + max);
      }
    return o;
  }
}
