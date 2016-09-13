/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.adobe;

import java.util.Hashtable;
import java.util.Dictionary;

public class Glyph
{
  String name;
  float width;
  double [] bbox;
  int defaultCode;
  Dictionary ligatures;
  Dictionary kernings;

  static class Ligature
  {
    String next;
    String result;
  }

  void addLigature (String next,String result)
  {
    if (ligatures == null)
      ligatures = new Hashtable ();
    ligatures.put (next,result);
  }

  void addKerning (String next,double kern)
  {
    if (kernings == null)
      kernings = new Hashtable ();
    kernings.put (next,new Double (kern));
  }
}
