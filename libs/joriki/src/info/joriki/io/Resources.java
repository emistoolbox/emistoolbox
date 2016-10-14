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
	  if (location == null)
		  return ClassLoader.getSystemResourceAsStream (resource); 

	  String packageName = location.getPackage ().getName ().replaceAll ("\\.","/"); 
	  return location.getClassLoader().getResourceAsStream(packageName + '/' + resource);
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
