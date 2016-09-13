/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.truetype;

import java.io.PrintStream;

public class ByteCodeDisassembler extends ByteCodeSpeaker 
{
  public ByteCodeDisassembler (PrintStream out)
  {
    super (out);
  }

  void operand (int operand)
  {
    log.print (" " + Integer.toHexString (operand & 0xffff));
  }

  public void disassemble (byte [] b)
  {
    disassemble (new ByteCodeStream (b,0));
  }

  public void disassemble (ByteCodeStream codeStream)
  {
    int opcode;

    while ((opcode = codeStream.read ()) != -1 && opcode != 0x2d) // ENDF
      {
        log (opcode);
        if ((opcode & 0xf0) == 0xb0) // PUSH{B,W}[abc]
          {
            boolean words = (opcode & 8) == 8;
            opcode &= 7;
            while (opcode-- >= 0)
              operand (words ? codeStream.readWord () : codeStream.read ());
          }
        else if ((opcode & 0xfe) == 0x40) // NPUSH
          {
            boolean words = (opcode & 1) == 1;
            int n = codeStream.read ();
            operand (n);
            while (n-- > 0)
              operand (words ? codeStream.readWord () : codeStream.read ());
          }
        log.println ();
      }
  }
}
