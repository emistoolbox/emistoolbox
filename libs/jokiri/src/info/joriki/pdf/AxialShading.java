/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import info.joriki.util.Assertions;

public class AxialShading extends LinearShading
{
  public float [] [] endpoint = new float [2] [2];

  AxialShading (PDFDictionary dictionary,ResourceResolver resourceResolver)
  {
    super (dictionary,resourceResolver);

    float [] coords = dictionary.getFloatArray ("Coords");
    Assertions.expect (coords.length,4);
    for (int i = 0,k = 0;i < 2;i++)
      for (int j = 0;j < 2;j++,k++)
        endpoint [i] [j] = coords [k];
    dictionary.checkUnused ("4.27");
  }
}
