/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import info.joriki.io.Outputable;

public class Version implements Comparable, Outputable
{
  public final int major;
  public final int minor;

  public Version (int major,int minor)
  {
    this.major = major;
    this.minor = minor;
  }

  public Version (String version)
  {
    int index = version.indexOf ('.');
    major = Integer.parseInt (version.substring (0,index));
    minor = Integer.parseInt (version.substring (index + 1));
  }

  public Version (DataInput in) throws IOException
  {
    major = in.readUnsignedShort ();
    minor = in.readUnsignedShort ();
  }

  public void writeTo (DataOutput out) throws IOException
  {
    out.writeShort (major);
    out.writeShort (minor);
  }

  public String toString ()
  {
    return major + "." + minor;
  }

  public boolean equals (Object o)
  {
    try {
      return compareTo (o) == 0;
    } catch (ClassCastException cce) {
      return false;
    }
  }

  public int compareTo (Object o)
  {
    Version v = (Version) o;
    return major > v.major ? 1 : minor - v.minor;
  }

  public boolean moreRecentThan (Version v)
  {
    return compareTo (v) > 0;
  }
}
