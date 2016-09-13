/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.jpeg;

import info.joriki.io.filter.Crank;
import info.joriki.io.filter.OutputFilter;
import info.joriki.io.filter.Concatenator;
import info.joriki.io.filter.SinkRecipient;

public class JPEGConcatenator
{
  public static SinkRecipient concatenate (JPEGSpeaker ... arr)
  {
    OutputFilter chain = null;
    JPEGFormat format = ((JPEGSinkRecipient) arr [0]).getFormat ();
    for (int i = 1;i < arr.length;i++)
      {
        ((JPEGSourceRecipient) arr [i]).setFormat (format);
        if (!(arr [i] instanceof JPEGCrank && ((JPEGCrank) arr [i]).isTrivial ()))
          {
            JPEGBuffer buffer = new JPEGBuffer (format);
            if (i != arr.length - 1)
              format = ((JPEGSinkRecipient) arr [i]).getFormat ();
            OutputFilter filter = Concatenator.concatenate (buffer,(Crank) arr [i]);
            chain = chain == null ? filter : Concatenator.concatenate (chain,filter);
          }
      }

    SinkRecipient head = (SinkRecipient) arr [0];
    if (chain == null)
      return head;
    head.setSink (chain.getSink ());
    return chain;
  }
}
