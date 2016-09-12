/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.font;

public class FontSpecification
{
  public String family;
  public String weight;
  public String style;
  
  final static int SERIF = 0;
  final static int SANS  = 1;
  final static int FIXED = 2;

  final static String [] genericNames = {"serif","sans-serif","monospace"};
  final static String [] specificNames = {"Times","Helvetica","Courier"};
  /*
    By experimenting a lot I found that VML text is positioned as follows:
    In a span, the distance from "top" to the baseline is the ascent.
    In a textpath, the distance from the path to the baseline is
    half the point size minus the descent:

    --------------------- top
    |
    |
    ascent
    ---------------|----- path
    |    |
    ----------|---------- baseline
    |    1/2
    descent  |
    |     |
    --------------------- top of next line

    Note that the distance from the top to the path is generally not 1/2.

    These are from times.ttf, arial.ttf and cour.ttf respectively:
  */
  final static int [] ascents = {1825,1854,1705};
  final static int [] descents = {443,434,615};
  final static double fontScale = 1 / 2048.;

  String strip (String suffix)
  {
    if (family.endsWith (suffix))
      {
        family = family.substring (0,family.length () - suffix.length ());
        return suffix;
      }
    return null;
  }

  public FontSpecification () {}

  public FontSpecification (String fontName)
  {
    family = fontName;
    style = strip ("Oblique");
    if (style == null)
      style = strip ("Italic");
    weight = strip ("Bold");

    if (family.indexOf ('-') != -1)
      {
        strip ("Roman");
        strip ("-");
      }
    strip (",");
  }

  public String toString ()
  {
    StringBuilder stringBuilder = new StringBuilder ();
    stringBuilder.append ("family: ").append (family);
    if (weight != null)
      stringBuilder.append (" weight: ").append (weight);
    if (style != null)
      stringBuilder.append (" style: ").append (style);
    return stringBuilder.toString ();
  }

  public String toPostScriptName ()
  {
    StringBuilder nameBuilder = new StringBuilder ();
    nameBuilder.append (family);
    if (family.equals ("Times") || weight != null || style != null)
      nameBuilder.append ('-');
    if (family.equals ("Times") && weight == null && style == null)
      nameBuilder.append ("Roman");
    else
      {
        if (weight != null)
          nameBuilder.append (weight);
        if (style != null)
          nameBuilder.append (style);
      }
    return nameBuilder.toString ();
  }

  public void standardize ()
  {
    if (family.equals ("Helvetica") && "Italic".equals (style))
      style = "Oblique";
  }

  public double getAscent ()
  {
    return fontScale * ascents [getFontIndex ()];
  }

  public double getDescent ()
  {
    return fontScale * descents [getFontIndex ()];
  }

  private int getFontIndex ()
  {
    for (int i = 0;i < 3;i++)
      if (family.equals (genericNames [i]) ||
          family.equals (specificNames [i]))
        return i;
    return -1;
  }
}
