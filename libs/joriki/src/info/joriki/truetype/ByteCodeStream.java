/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.truetype;

import info.joriki.io.ByteStream;

import info.joriki.util.Assertions;

public class ByteCodeStream extends ByteStream
{
  ByteCodeStream (byte [] buf,int pos)
  {
    super (buf,pos);
  }

  public int readWord ()
  {
    byte hi = buf [pos++];
    byte lo = buf [pos++];
    return (hi << 8) | (lo & 0xff); // hi is sign-extended
  }

  public void jumpRelative (int offset)
  {
    pos += offset - 1;
  }

  public int skip ()
  {
    int opcode = read ();
    Assertions.unexpect (opcode,-1);
    if ((opcode & ~1) == 0x40) // NPUSHB[], NPUSHW[]
      {
        int narg = read ();
        Assertions.unexpect (narg,-1);
        pos += narg << (opcode & 1);
      }
    else if ((opcode & 0xf0) == 0xb0) // PUSHB[abc], PUSHW [abc]
      pos += ((opcode & 7) + 1) << ((opcode >> 3) & 1);
    return opcode;
  }

  public void skipConditional ()
  {
    for (int nest = 1;nest > 0;)
      switch (skip ())
        {
        case 0x1b : if (nest == 1) return; break; // ELSE[]
        case 0x58 : nest++; break; // IF[]
        case 0x59 : nest--; break; // EIF[]
        }
  }

  String positionString ()
  {
    return "at " + Integer.toHexString (pos);
  }
}
