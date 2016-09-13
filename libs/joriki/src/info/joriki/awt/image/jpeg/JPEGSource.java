/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.jpeg;

import java.io.IOException;

public interface JPEGSource
{
  void deallocationRequest (JPEGRequest request); 
  int readRequest (JPEGRequest request) throws IOException;
}
