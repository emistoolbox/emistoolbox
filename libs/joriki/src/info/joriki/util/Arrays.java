/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.util;

public class Arrays
{
  private Arrays () {}

  final static int initFill = 8;

  static public void clear (Object arr)
  {
    if (arr instanceof Object [])
      fill ((Object []) arr,null);
    else if (arr instanceof boolean [])
      fill ((boolean []) arr,false);
    else
      fill (arr,0);
  }

  static public void fill (Object arr,double val)
  {
    if (arr instanceof double [])
      fill ((double []) arr,val);
    else if (arr instanceof float [])
      fill ((float []) arr,(float) val);
    else if (arr instanceof long [])
      fill ((long []) arr,(long) val);
    else if (arr instanceof int [])
      fill ((int []) arr,(int) val);
    else if (arr instanceof short [])
      fill ((short []) arr,(short) val);
    else if (arr instanceof char [])
      fill ((char []) arr,(char) val);
    else if (arr instanceof byte [])
      fill ((byte []) arr,(byte) val);
    else
      throw new ArrayStoreException ("can't fill non-numeric array");
  }

  static public void fill (double [] arr,double val)
  {
    for (int i = 0;i < arr.length && i < initFill;i++)
      arr [i] = val;
    completeFill (arr,arr.length);
  }

  static public void fill (float [] arr,float val)
  {
    for (int i = 0;i < arr.length && i < initFill;i++)
      arr [i] = val;
    completeFill (arr,arr.length);
  }

  static public void fill (long [] arr,long val)
  {
    for (int i = 0;i < arr.length && i < initFill;i++)
      arr [i] = val;
    completeFill (arr,arr.length);
  }

  static public void fill (int [] arr,int val)
  {
    for (int i = 0;i < arr.length && i < initFill;i++)
      arr [i] = val;
    completeFill (arr,arr.length);
  }

  static public void fill (short [] arr,short val)
  {
    for (int i = 0;i < arr.length && i < initFill;i++)
      arr [i] = val;
    completeFill (arr,arr.length);
  }

  static public void fill (char [] arr,char val)
  {
    for (int i = 0;i < arr.length && i < initFill;i++)
      arr [i] = val;
    completeFill (arr,arr.length);
  }

  static public void fill (byte [] arr,byte val)
  {
    for (int i = 0;i < arr.length && i < initFill;i++)
      arr [i] = val;
    completeFill (arr,arr.length);
  }

  static public void fill (boolean [] arr,boolean val)
  {
    for (int i = 0;i < arr.length && i < initFill;i++)
      arr [i] = val;
    completeFill (arr,arr.length);
  }

  static public void fill (Object [] arr,Object val)
  {
    for (int i = 0;i < arr.length && i < initFill;i++)
      arr [i] = val;
    completeFill (arr,arr.length);
  }

  static private void completeFill (Object arr,int len)
  {
    int done = initFill >> 1;
    while ((done <<= 1) < len)
      System.arraycopy (arr,0,arr,done,Math.min (done,len - done));
  }

  static public boolean equals (byte [] b1,byte [] b2)
  {
    if (b1.length != b2.length)
      return false;

    for (int i = 0;i < b1.length;i++)
      if (b1 [i] != b2 [i])
        return false;

    return true;
  }

  static public boolean equals (double [] b1,double [] b2)
  {
    if (b1.length != b2.length)
      return false;

    for (int i = 0;i < b1.length;i++)
      if (b1 [i] != b2 [i])
        return false;

    return true;
  }

  static public int [] extend (int [] arr,int len)
  {
    int [] ext = new int [len];
    System.arraycopy (arr,0,ext,0,Math.min (arr.length,len));
    return ext;
  }

  static public void scale (double [] arr,double s)
  {
    for (int i = 0;i < arr.length;i++)
      arr [i] *= s;
  }

  static public void scale (float [] arr,double s)
  {
    for (int i = 0;i < arr.length;i++)
      arr [i] *= s;
  }

  static public Object max (Valuable [] valuables)
  {
    Object max = null;
    double maxValue = Double.MIN_VALUE;
    for (int i = 0;i < valuables.length;i++)
      {
        Valuable valuable = valuables [i];
        double value = valuable.value ();
        if (value > maxValue)
          {
            max = valuable;
            maxValue = value;
          }
      }
    return max;
  }

  public static boolean isPrefix (byte [] prefix,byte [] sequence) {
    for (int i = 0;i < prefix.length;i++)
      if (prefix [i] != sequence [i])
        return false;
    return true;
  }
}
