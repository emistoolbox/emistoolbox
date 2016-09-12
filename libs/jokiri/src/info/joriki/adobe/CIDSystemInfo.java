/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.adobe;

public class CIDSystemInfo
{
  public String registry;
  public String ordering;
  public int supplement;

  public CIDSystemInfo (String registry,String ordering,int supplement)
  {
    this.registry = registry;
    this.ordering = ordering;
    this.supplement = supplement;
  }

  public boolean compatibleWith (CIDSystemInfo c)
  {
    return
      registry.equals (c.registry) &&
      ordering.equals (c.ordering);
  }
}
