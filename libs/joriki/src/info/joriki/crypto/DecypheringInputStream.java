/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.crypto;

import java.io.IOException;
import java.io.InputStream;

import info.joriki.io.ReadableFilterInputStream;

public class DecypheringInputStream extends ReadableFilterInputStream
{
  StreamCypher cypher;

  public DecypheringInputStream (InputStream in,StreamCypher cypher)
  {
    super (in);
    this.cypher = cypher;
  }

  public int read () throws IOException
  {
    int b = in.read ();
    return b == -1 ? -1 : (cypher.decrypt (b) & 0xff);
  }
  
  public int read (byte [] b,int off,int len) throws IOException
  {
    int read = in.read (b,off,len);
    for (int lim = off + read;off < lim;off++)
      b [off] = cypher.decrypt (b [off]);
    return read;
  }

  // We need to get the cypher into the same state as if we'd actually read the bytes.
  // On the assumption that the state of the cypher depends only on the byte count and
  // not on the actual bytes, as is the case for the arc4 cypher, we could skip the
  // bytes using super.skip () and just advance the cypher (either by introducing a
  // new advance () method or by feeding it dummy bytes), but the benefit is probably
  // negligible and other cyphers might not have this property. Thus we read the bytes.
  public long skip (long n) throws IOException {
    long skipped = 0;
    int b;
    while (skipped < n && (b = read ()) != -1)
      skipped++;
    return skipped;
  }
}
