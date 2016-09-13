/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.jpeg;

import java.io.IOException;

public interface JPEGSink
{
  void allocationRequest (JPEGRequest request); 
  void writeRequest (JPEGRequest request) throws IOException;
  void setFormat (JPEGFormat format);
}
