/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import info.joriki.util.Assertions;
import info.joriki.util.NotImplementedException;

// this is *not* an axial (a.k.a. gradient) shading --
// it's linear in the sense of one-dimensional, as opposed
// to the remaining, two-dimensional shading types.
// Its descendants are axial shadings and radial shadings.
public class LinearShading extends DomainShading
{
  public boolean [] extend;

  LinearShading (PDFDictionary dictionary,ResourceResolver resourceResolver)
  {
    super (dictionary,resourceResolver,1);

    extend = dictionary.getBooleanArray ("Extend",new boolean [] {false,false});
    Assertions.expect (extend.length,2);

    Assertions.expect (domain [0] != domain [1]);

    if (domain [0] > domain [1])
      throw new NotImplementedException ("inverted domain for linear shading");
  }
}
