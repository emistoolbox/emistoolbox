/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.charstring;

import java.io.ByteArrayOutputStream;

import info.joriki.io.ByteStream;

import info.joriki.util.Assertions;

public class CharStringWriter
  extends ByteArrayOutputStream
  implements CharStringSpeaker
{
  int type;

  public CharStringWriter (int type)
  {
    this.type = type;
  }

  public void command (int c,ByteStream byteStream)
  {
    write (c);
  }

  public void escape (int e)
  {
    write (12);
    write (e);
  }

  public void argument (double a)
  {
    if (a != (int) a)
      {
        Assertions.expect (type,2);
        Assertions.limit (a,-0x8000,0x8000);
        writeInt (Math.round ((float) (a * 0x10000)));
      }
    else
      {
        int r = (int) a;

        if (-107 <= r && r <= 107)
          write (r + 139);
        else if (108 <= r && r <= 1131)
          write (r,247);
        else if (-1131 <= r && r <= -108)
          write (-r,251);
        else if (type == 2 && -0x8000 <= r && r < 0x8000)
          {
            write (28);
            write (r >> 8);
            write (r);
          }
        else
          {
            Assertions.expect (type,1);
            writeInt (r);
          }
      }
  }

  private void writeInt (int i)
  {
    write (255);
    for (int j = 24;j >= 0;j -= 8)
      write (i >> j);
  }

  private void write (int r,int off)
  {
    r -= 108;
    write (off + (r >> 8));
    write (r);
  }
}
