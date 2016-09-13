/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class Resources
{
  public static InputStream getInputStream (Class location,String resource) {
    return ClassLoader.getSystemResourceAsStream (location == null ? resource :
    location.getPackage ().getName ().replaceAll ("\\.","/") + '/' + resource);
  }
  
  public static Reader getReader (Class location,String resource) {
    InputStream inputStream = getInputStream (location,resource);
    return inputStream == null ? null : new InputStreamReader (inputStream);
  }

  public static byte [] getBytes (Class location,String resource) throws IOException {
    InputStream inputStream = getInputStream (location,resource);
    return inputStream == null ? null : Util.undump (inputStream);
  }
}
