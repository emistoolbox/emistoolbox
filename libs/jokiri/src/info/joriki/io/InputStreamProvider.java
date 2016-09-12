/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.InputStream;
import java.io.IOException;

/**
   An input stream provider abstracts an object such as a file or a URL
   that may be able to provide several identical input streams.
*/
public interface InputStreamProvider
{
  /**
     Returns an input stream provided by this provider.
     The first call to this method must return an input stream.
     Any further calls should return input streams with identical
     content, or <code>null</code> if this is not possible.
     @return an input stream provided by this provider
     @exception IOException if an I/O error occurs
  */
  InputStream getInputStream () throws IOException;
  
  byte [] toByteArray () throws IOException;
}
