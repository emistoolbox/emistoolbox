/*
 * Copyright 2006 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.crypto;

import java.io.IOException;
import java.io.OutputStream;

import info.joriki.io.WriteableOutputStream;

public class EncypheringOutputStream extends WriteableOutputStream
{
  OutputStream out;
  StreamCypher cypher;
  byte [] buf = new byte [0];

  public EncypheringOutputStream (OutputStream out,StreamCypher cypher)
  {
    this.out = out;
    this.cypher = cypher;
  }

  public void write (int b) throws IOException
  {
    out.write (cypher.encrypt (b));
  }

  public void write (byte [] b,int off,int len) throws IOException
  {
    if (buf.length < len)
      buf = new byte [len];
    for (int i = 0,j = off;i < len;i++,j++)
      buf [i] = cypher.encrypt (b [j]);
    out.write (buf,0,len);
  }
}
