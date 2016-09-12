/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.crypto;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;

import info.joriki.util.General;

abstract public class AbstractDigest implements Digest
{
  public void digest (InputStream in) throws IOException
  {
    for (;;)
      {
	int b = in.read ();
	if (b == -1)
	  return;
	digest (b);
      }
  }

  public void digest (byte [] b)
  {
    digest (b,0,b.length);
  }

  public void digest (byte [] b,int off,int len)
  {
    for (int i = 0;i < len;i++)
      digest (b [off++]);
  }

  public void digest (int b,int n)
  {
    while (n-- > 0)
      {
        digest (b);
        b >>= 8;
      }
  }

  public String getDigestString (String file) throws IOException
  {
    return getDigestString (new FileInputStream (file));
  }

  public String getDigestString (File file) throws IOException
  {
    return getDigestString (new FileInputStream (file));
  }

  public String getDigestString (InputStream in) throws IOException
  {
    reset ();
    digest (new BufferedInputStream (in));
    return getDigestString ();
  }

  public String getDigestString (byte [] arr)
  {
    reset ();
    digest (arr);
    return getDigestString ();
  }

  public String getDigestString ()
  {
    byte [] result = getDigest ();
    StringBuilder digestBuilder = new StringBuilder ();
    for (int j = 0;j < result.length;j++)
      digestBuilder.append (General.zeroPad
                     (Integer.toHexString (result [j] & 0xff),2));
    return digestBuilder.toString ();
  }
}
