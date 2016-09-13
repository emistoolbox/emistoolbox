/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.sfnt;

import java.io.PrintStream;

import java.util.Iterator;

import info.joriki.io.Outputable;

import info.joriki.util.Range;

abstract public class CMapTable
  extends EncodingScheme
  implements Outputable, CharacterMap
{
  public CMapTable () {}

  public CMapTable (int platform,int encoding,int language)
  {
    super (platform,encoding,language);
  }

  public CMapTable (CMapTable t)
  {
    super (t);
  }

  Iterator pairIterator ()
  {
    return new EncodingPairIterator (getMap ());
  }

  public int [] getUnicodes ()
  {
    int [] unicodes = new int [256];
    Iterator iterator = pairIterator ();
    while (iterator.hasNext ())
      {
        EncodingPair pair = (EncodingPair) iterator.next ();
        unicodes [pair.glyphIndex] = pair.code;
      }
    return unicodes;
  }

  public void printTo (PrintStream out)
  {
    Iterator iterator = pairIterator ();
    while (iterator.hasNext ())
      {
        EncodingPair pair = (EncodingPair) iterator.next ();
        System.out.println (pair.code + " (" + info.joriki.util.General.zeroPad (Integer.toHexString (pair.code),4) + ") : " + pair.glyphIndex);
      }
  }

  abstract public Range getDomain ();
}
