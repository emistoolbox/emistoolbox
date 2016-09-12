/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.util;

public class ArgumentIterator
{
  String [] arguments;
  int index;

  public ArgumentIterator (String [] arguments)
  {
    this.arguments = arguments;
  }

  public boolean hasNext ()
  {
    return index < arguments.length;
  }
  
  public boolean atOption ()
  {
    return hasNext () && arguments [index].charAt (0) == '-';
  }

  public String nextString ()
  {
    return arguments [index++];
  }

  public int nextInt ()
  {
    return General.intValue (nextString ());
  }

  public byte nextByte ()
  {
    return Byte.parseByte (nextString ());
  }

  public boolean nextBoolean ()
  {
    return new Boolean (nextString ()).booleanValue ();
  }

  public float nextFloat ()
  {
    return Float.parseFloat (nextString ());
  }

  public double nextDouble ()
  {
    return Double.parseDouble (nextString ());
  }
}
