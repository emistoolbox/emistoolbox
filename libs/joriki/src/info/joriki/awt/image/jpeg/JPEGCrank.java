/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.jpeg;

import info.joriki.io.filter.Crank;

abstract public class JPEGCrank implements JPEGSourceRecipient,JPEGSinkRecipient,Crank
{
  JPEGSink sink;
  JPEGSource source;

  JPEGFormat inputFormat;
  JPEGFormat outputFormat;

  public void setSink (Object sink)
  {
    this.sink = (JPEGSink) sink;
  }

  public void setSource (Object source)
  {
    this.source = (JPEGSource) source;
  }

  public JPEGFormat getFormat ()
  {
    return outputFormat;
  }

  abstract public boolean isTrivial ();
}
