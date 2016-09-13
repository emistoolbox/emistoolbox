/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.sfnt;

import java.util.Map;
import java.util.Iterator;

public class EncodingPairIterator implements Iterator
{
  // constructs an iterator over EncodingPairs from a Character -> Integer map
  final Iterator mapIterator;

  public EncodingPairIterator (Map map)
  {
    mapIterator = map.entrySet ().iterator ();
  }

  public boolean hasNext ()
  {
    return mapIterator.hasNext ();
  }

  public Object next ()
  {
    Map.Entry entry = (Map.Entry) mapIterator.next ();
    return new EncodingPair
      (((Integer) entry.getKey ()).intValue (),
       ((Integer) entry.getValue ()).intValue ());
  }

  public void remove ()
  {
    mapIterator.remove ();
  }
}
