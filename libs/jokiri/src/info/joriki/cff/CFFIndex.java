/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.cff;

import java.io.IOException;
import java.io.ByteArrayOutputStream;

import java.util.ArrayList;

import info.joriki.io.Util;
import info.joriki.io.FullySeekableDataInput;

import info.joriki.util.General;

public class CFFIndex extends ArrayList implements CFFObject
{
  CFFIndex () {}

  CFFIndex (FullySeekableDataInput in) throws IOException
  {
    readFrom (in);
  }

  void readFrom (FullySeekableDataInput in) throws IOException
  {
    int n = in.readShort ();
    if (n == 0)
      return;
    int offsize = in.read ();
    int [] offsets = new int [n + 1];
      
    for (int i = 0;i <= n;i++)
      {
        int off = 0;
        for (int k = 0;k < offsize;k++)
          off = (off << 8) + in.read ();
        offsets [i] = off;
      }

    for (int i = 0;i < n;i++)
      add (Util.readBytes (in,offsets [i+1] - offsets [i]));
  }

  public void writeTo (ByteArrayOutputStream baos)
  {
    int n = size ();
    if (n == 0)
      {
        baos.write (0);
        baos.write (0);
        return;
      }
    int [] offsets = new int [n + 1];
    byte [] [] data = new byte [n] [];
    offsets [0] = 1;
    for (int i = 0;i < n;i++)
      {
        Object o = get (i);
        if (o instanceof CFFObject)
          {
            ByteArrayOutputStream subbaos = new ByteArrayOutputStream ();
            ((CFFObject) o).writeTo (subbaos);
            data [i] = subbaos.toByteArray ();
          }
        else if (o instanceof byte [])
          data [i] = (byte []) o;
        else
          data [i] = o.toString ().getBytes ();
  
        offsets [i+1] = offsets [i] + data [i].length;
      }

    int offsize = General.countBytes (offsets [n]);

    baos.write (n >> 8);
    baos.write (n);
    baos.write (offsize);

    for (int i = 0;i <= n;i++)
      {
        int b = offsize;
        int c = offsets [i];
        while (b > 0)
          baos.write (c >> (--b << 3));
      }
    try {
      for (int i = 0;i < n;i++)
        baos.write (data [i]);
    } catch (IOException ioe) {} // can't happen
  }

  public byte [] [] getSubroutines ()
  {
    byte [] [] subroutines = new byte [size ()] [];
    for (int i = 0;i < subroutines.length;i++)
      subroutines [i] = (byte []) get (i);
    return subroutines;
  }
}
