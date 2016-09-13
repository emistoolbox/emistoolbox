/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.ByteArrayInputStream;

public class CIDToGIDMap
{
  char [] GIDs;

  public CIDToGIDMap (PDFStream stream) throws IOException
  {
    byte [] data = stream.getData ("3.4");
    DataInputStream dis = new DataInputStream (new ByteArrayInputStream (data));
    GIDs = new char [data.length >> 1];

    for (int i = 0;i < GIDs.length;i++)
      GIDs [i] = dis.readChar ();
  }

  public char map (int code)
  {
    return GIDs [code];
  }
}
