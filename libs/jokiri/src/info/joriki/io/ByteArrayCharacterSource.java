/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.io;

abstract public class ByteArrayCharacterSource implements CharacterSource
{
  public int pos = 0;
  public byte [] arr;
  
  public ByteArrayCharacterSource (byte [] arr)
  {
    this.arr = arr;
  }

  // to announce it doesn't throw IOExceptions
  abstract public int read ();
}
