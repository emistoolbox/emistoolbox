/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.truetype;

public class Subroutine
{
  byte [] code;
  int offset;
  
  Subroutine (ByteCodeStream codeStream)
  {
    code = codeStream.buf;
    offset = codeStream.pos;
  }
  
  ByteCodeStream getCodeStream ()
  {
    return new ByteCodeStream (code,offset);
  }
}

