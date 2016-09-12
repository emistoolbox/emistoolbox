/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.charstring;

import info.joriki.io.ByteStream;

public class CharStringReader
{
  int type;

  public CharStringReader (int type)
  {
    this.type = type;
  }

  public void read (byte [] routine,CharStringSpeaker speaker)
  {
    ByteStream byteStream = new ByteStream (routine);
    int r;
    while ((r = byteStream.read ()) != -1)
      if (r == 12)
        speaker.escape (byteStream.readByte () & 0xff);
      else if (r == 28 && type == 2)
        {
          r = byteStream.readByte (); // intentional sign extension
          speaker.argument ((r << 8) | byteStream.read ());
        }
      else if (r < 32)
        {
          speaker.command (r,byteStream);
          if (r == 11)
            return;
        }
      else if (r < 247)
        speaker.argument (r - 139);
      else if (r < 251)
        speaker.argument (((r - 247) << 8) + byteStream.read () + 108);
      else if (r < 255)
        speaker.argument (-(((r - 251) << 8) + byteStream.read () + 108));
      else // 255
        {
          r = byteStream.read ();
          r = (r << 8) | byteStream.read ();
          r = (r << 8) | byteStream.read ();
          r = (r << 8) | byteStream.read ();
          speaker.argument (type == 2 ? r / (double) 0x10000 : r);
        }
  }
}
