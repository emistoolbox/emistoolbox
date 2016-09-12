/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.awt.image.jpeg;

abstract public class JPEGInitial implements JPEGSinkRecipient
{
  JPEGSink sink;

  public void setSink (Object sink)
  {
    this.sink = (JPEGSink) sink;
  }
}
