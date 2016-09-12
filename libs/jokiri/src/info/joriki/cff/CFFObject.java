/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.cff;

import java.io.ByteArrayOutputStream;

public interface CFFObject
{
  public void writeTo (ByteArrayOutputStream baos);
}
