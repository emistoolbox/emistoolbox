/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import info.joriki.util.Assertions;

public class DomainShading extends PDFShading
{
  public float [] domain;
  
  DomainShading (PDFDictionary dictionary,ResourceResolver resourceResolver,int ninput)
  {
    super (dictionary,resourceResolver,ninput);

    domain = dictionary.getFloatArray ("Domain");
    if (domain == null)
      {
        domain = new float [2 * ninput];
        for (int i = 0;i < ninput;i++)
          domain [2 * i + 1] = 1;
      }

    checkDomain (domain);

    Assertions.expect (hasFunction ());
    Assertions.expect (domain.length,2 * ninput);
  }
}
