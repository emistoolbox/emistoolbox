/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.sfnt;

import java.util.TreeMap;
import java.util.SortedMap;

abstract public class EncodingDependentTable extends SFNTTable
{
  SortedMap entries = new TreeMap ();

  public EncodingDependentTable (String id)
  {
    super (id);
  }

  public void addEntry (EncodingScheme scheme)
  {
    entries.put (scheme,scheme);
  }

  public EncodingScheme get (EncodingScheme scheme)
  {
    return (EncodingScheme) entries.get (scheme);
  }
}
