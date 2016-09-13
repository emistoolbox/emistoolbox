/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.util;

import java.lang.ref.SoftReference;

public abstract class SmartSoftReference
{
  SoftReference dumbReference;
  
  abstract protected Object construct ();

  public void put (Object object)
  {
    dumbReference = new SoftReference (object);
  }
  
  public Object get ()
  {
    Object object = null;
    if (dumbReference != null)
      object = dumbReference.get ();
    if (object == null)
      put (object = construct ());
    return object;
  }
}
