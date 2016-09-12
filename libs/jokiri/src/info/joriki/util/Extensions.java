/*
 * Copyright 2003 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.util;

public class Extensions
{
  public static String extension (String name)
  {
    int dot = name.lastIndexOf ('.');
    return dot == -1 ? null : name.substring (dot+1);
  }

  public static String replaceExtension (String name,String old,String now)
  {
    if (!name.toLowerCase ().endsWith (old.toLowerCase ()))
      throw new IllegalArgumentException (name + " does not end in " + old);
    return name.substring (0,name.length () - old.length ()) + now;
  }

  public static String stripExtension (String name,String ext)
  {
    return replaceExtension (name,ext,"");
  }

  public static String stripExtension (String name)
  {
    int dot = name.lastIndexOf ('.');
    return dot == -1 ? name : name.substring (0,dot);
  }

  public static String incrementFileName (String name)
  {
    String ext = extension (name);
    name = stripExtension (name);
    int last = name.length () - 1;
    return name.substring (0,last) +
      ((char) (name.charAt (last) + 1)) +
      '.' + ext;
  }
}
